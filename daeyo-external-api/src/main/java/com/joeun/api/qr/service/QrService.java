package com.joeun.api.qr.service;

import com.joeun.api.security.TenantProvider;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QrService {

    @Value("${qr.jwt.secret}")
    private String secret; // 최소 64바이트 이상 권장

    // 기본 TTL(초). 짧게 운용(예: 180=3분). 요청마다 오버라이드 가능.
    @Value("${qr.jwt.default-ttl-seconds:180}")
    private long defaultTtlSeconds;

    @Value("${qr.scheme:daeyo}")
    private String scheme;

    @Value("${qr.size:320}")
    private int size;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /** 관리자 조직용 토큰 생성 (TTL 오버라이드 가능) */
    public String buildOrgToken(Long u, Long o, String type, Long siteId, Long ttlSecOverride) {
        long ttl = (ttlSecOverride == null || ttlSecOverride <= 0) ? defaultTtlSeconds : ttlSecOverride;

        Instant now = Instant.now();
        Date iat = Date.from(now);
        Date exp = Date.from(now.plusSeconds(ttl));

        Map<String, Object> claims = new HashMap<>();
        claims.put("t", (type == null || type.isBlank()) ? "SITE" : type);
        claims.put("u", u);
        claims.put("o", o);
        if (siteId != null) claims.put("sid", siteId);
        // 캐시 무효화/리프레시 구분용 식별자
        claims.put("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .setSubject("daeyo-qr")
                .addClaims(claims)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String qrContent(String token) {
        String enc = URLEncoder.encode(token, StandardCharsets.UTF_8);
        // ex) daeyo://site?token=xxxxx
        return scheme + "://site?token=" + enc;
    }

    /** PNG 생성 (TTL 오버라이드 가능) */
    public byte[] generateOrgQrPng(Long u, Long o, String type, Long siteId, Long ttlSecOverride) {
        try {
            String token = buildOrgToken(u, o, type, siteId, ttlSecOverride);
            String content = qrContent(token);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new IllegalStateException("QR generation failed", e);
        }
    }

    /** 스캔 후 서버 검증 */
    public Map<String, Object> validateToken(String token) {
        var jws = Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token);

        var c = jws.getBody();
        Number u = (Number) c.get("u");
        Number o = (Number) c.get("o");
        Number sid = (Number) c.get("sid");

        Map<String, Object> result = new HashMap<>();
        result.put("valid", true);
        result.put("type", c.get("t"));
        result.put("universityId", u == null ? null : u.longValue());
        result.put("organizationId", o == null ? null : o.longValue());
        if (sid != null) result.put("siteId", sid.longValue());
        result.put("issuedAt", c.getIssuedAt());
        result.put("expiresAt", c.getExpiration());
        result.put("jti", c.get("jti"));
        return result;
    }
}
