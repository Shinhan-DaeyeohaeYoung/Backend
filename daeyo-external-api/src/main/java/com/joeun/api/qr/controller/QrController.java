package com.joeun.api.qr.controller;

import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.api.qr.service.QrService;
import com.joeun.global.config.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@SecurityRequirement(name = "Authorization")
@Tag(name ="QR" ,description = "QR 발급 및 검증 API")
public class QrController {

    private final QrService qrService;
    private final OrganizationService organizationService;

    private static boolean isAdminRole(String role) {
        return role != null && (role.equals("ORG_ADMIN") || role.equals("ADMIN"));
    }

    private MyOrganizationResponse pickMyOrg(LoginUser loginUser, Long organizationId) {
        var memberships = organizationService.getMyOrganizations(loginUser, "");

        if (organizationId != null) {
            return memberships.stream()
                    .filter(m -> Objects.equals(m.getOrganizationId(), organizationId))
                    .findFirst()
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Not a member of organizationId=" + organizationId));
        }

        if (memberships.size() == 1) {
            return memberships.get(0);
        }

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "organizationId is required (memberships=" + memberships.size() + ")"
        );
    }



    /** (관리자) 조직 QR PNG 발급 — 짧은 TTL + 리프레시용 */
    @Operation(summary = "조직 QR 이미지(PNG) 발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "PNG 이미지 반환",
                    content = @Content(mediaType = "image/png",
                            schema = @Schema(type = "string", format = "binary")))
    })
    @GetMapping(value = "/admin/org-qr.png", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> orgQrPng(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(defaultValue = "SITE") String type,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long ttlSec,
            @RequestParam(required = false) Long nonce
    ) {
        var m = pickMyOrg(loginUser, organizationId);
        Long u = m.getUniversityId();
        Long o = m.getOrganizationId();

        byte[] png = qrService.generateOrgQrPng(u, o, type, siteId, ttlSec);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setCacheControl(CacheControl.noStore().mustRevalidate());
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        if (nonce != null) headers.add("X-Nonce", String.valueOf(nonce));

        return new ResponseEntity<>(png, headers, HttpStatus.OK);
    }

    // QrController.java
    @Operation(summary = "조직 QR 메타(JSON) 발급")
    @GetMapping("/admin/org-qr/meta")
    public Map<String, Object> orgQrMeta(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestParam(required = false) Long organizationId,
            @RequestParam(defaultValue = "SITE") String type,
            @RequestParam(required = false) Long siteId,
            @RequestParam(required = false) Long ttlSec
    ) {
        var m = pickMyOrg(loginUser, organizationId);
        Long u = m.getUniversityId();
        Long o = m.getOrganizationId();

        String token = qrService.buildOrgToken(u, o, type, siteId, ttlSec);
        var resolved = qrService.validateToken(token);

        var body = new LinkedHashMap<String, Object>();
        body.put("token", token);
        body.put("type", resolved.get("type"));
        body.put("universityId", resolved.get("universityId"));
        body.put("organizationId", resolved.get("organizationId"));
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
