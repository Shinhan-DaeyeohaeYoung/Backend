package com.joeun.api.qr.controller;

import com.joeun.api.qr.service.QrService;
import com.joeun.api.security.TenantProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name ="QR" ,description = "QR 발급 및 검증 API")
public class QrController {

    private final QrService qrService;
    private final TenantProvider tenant;

    /** (관리자) 조직 QR PNG 발급 — 짧은 TTL + 리프레시용 */
    @Operation(
            summary = "조직 QR 이미지(PNG) 발급 (관리자)",
            description = """
        관리자의 소속 (universityId/organizationId)을 포함한 짧은 TTL의 토큰을 QR에 인코딩하여 PNG로 반환합니다.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PNG 이미지 반환",
                    content = @Content(mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "인증 필요 혹은 테넌트 식별 불가"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류")
    })
    @GetMapping(value = "/admin/org-qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> orgQrPng(@RequestParam(defaultValue = "SITE") String type,
                                           @RequestParam(required = false) Long siteId,
                                           @RequestParam(required = false) Long ttlSec,
                                           // 매 호출마다 캐시Bust용 임의 값(타임스탬프 등) 넘겨도 OK
                                           @RequestParam(required = false) Long nonce) {
        Long u = tenant.universityId();
        Long o = tenant.organizationId();

        byte[] png = qrService.generateOrgQrPng(u, o, type, siteId, ttlSec);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        // 캐시 무효화(브라우저/프록시)
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        // 매 요청마다 다른 nonce를 주면 CDN/브라우저 캐시도 회피
        if (nonce != null) {
            headers.add("X-Nonce", String.valueOf(nonce));
        }

        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }

    // QrController.java
    @Operation(
            summary = "조직 QR 메타(JSON) 발급 (관리자)",
            description = """
        QR에 들어갈 **토큰과 만료시각** 등을 JSON으로 반환합니다.
        프론트에서 토큰을 자체 QR로 렌더하거나, 카운트다운/갱신 UI에 활용하세요.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 메타 반환",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "ORG",
  "universityId": 1,
  "organizationId": 2,
  "issuedAt": "2025-08-25T01:20:30Z",
  "expiresAt": "2025-08-25T01:21:20Z"
}
"""))),
            @ApiResponse(responseCode = "401", description = "인증 필요 혹은 테넌트 식별 불가"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 오류")
    })
    @GetMapping("/admin/org-qr/meta")
    public Map<String, Object> orgQrMeta(@RequestParam(defaultValue = "SITE") String type,
                                         @RequestParam(required = false) Long siteId,
                                         @RequestParam(required = false) Long ttlSec) {
        var t = qrService.currentTenant();
        String token = qrService.buildOrgToken(t.get("u"), t.get("o"), type, siteId, ttlSec);
        var resolved = qrService.validateToken(token);

        var body = new java.util.LinkedHashMap<String, Object>();
        body.put("token", token);
        body.put("type", resolved.get("type"));
        body.put("universityId", resolved.get("universityId"));
        body.put("organizationId", resolved.get("organizationId"));
        // siteId는 null일 수 있으니 조건부로
        Object sid = resolved.get("siteId");
        if (sid != null) body.put("siteId", sid);
        body.put("issuedAt", resolved.get("issuedAt"));
        body.put("expiresAt", resolved.get("expiresAt"));
        return body;
    }

    /** (사용자 스캔 후) 토큰 검증 */
    @Operation(
            summary = "QR 토큰 검증 (사용자)",
            description = """
        사용자가 스캔하여 얻은 token을 검증합니다. 유효하면 조직/현장 정보를 반환합니다.
        클라이언트는 응답의 organizationId가 기대하는 곳인지 확인 후 다음 페이지로 이동하세요.
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검증 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
{
  "type": "ORG",
  "universityId": 1,
  "organizationId": 2,
  "issuedAt": "2025-08-25T01:20:30Z",
  "expiresAt": "2025-08-25T01:21:20Z"
}
"""))),
            @ApiResponse(responseCode = "400", description = "토큰 누락/형식 오류"),
            @ApiResponse(responseCode = "401", description = "만료/위조 등으로 검증 실패")
    })
    @PostMapping("/qrs/resolve")
    public Map<String, Object> resolve(@io.swagger.v3.oas.annotations.parameters.RequestBody(
                                                   required = true,
                                                   content = @Content(mediaType = "application/json",
                                                           examples = @ExampleObject(
                                                                   name = "TokenOnly",
                                                                   value = "{ \"token\": \"eyJhbGciOi...<JWT>...\" }"
                                                           )
                                                   )
                                           )
                                           @RequestBody Map<String, String> body

    ) {
        String token = body.get("token");
        var resolved = qrService.validateToken(token);

        var resp = new java.util.LinkedHashMap<String, Object>();
        resp.put("type", resolved.get("type"));
        resp.put("universityId", resolved.get("universityId"));
        resp.put("organizationId", resolved.get("organizationId"));
        Object sid = resolved.get("siteId");
        if (sid != null) resp.put("siteId", sid);
        resp.put("issuedAt", resolved.get("issuedAt"));
        resp.put("expiresAt", resolved.get("expiresAt"));
        return resp;
    }

}
