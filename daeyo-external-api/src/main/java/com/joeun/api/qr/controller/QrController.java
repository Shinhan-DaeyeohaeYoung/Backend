package com.joeun.api.qr.controller;

import com.joeun.api.qr.service.QrService;
import com.joeun.api.security.TenantProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class QrController {

    private final QrService qrService;
    private final TenantProvider tenant;

    /** (관리자) 조직 QR PNG 발급 — 짧은 TTL + 리프레시용 */
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
    @PostMapping("/qrs/resolve")
    public Map<String, Object> resolve(@RequestBody Map<String, String> body) {
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
