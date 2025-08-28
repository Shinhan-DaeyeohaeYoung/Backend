package com.joeun.global.util;

import com.joeun.domain.users.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-token-validity-seconds:3600}")
  private long accessTokenValiditySeconds;

  @Value("${jwt.refresh-token-validity-seconds:1209600}") // 14일 (선택)
  private long refreshTokenValiditySeconds;

  public String generateAccessToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim("universityId", user.getUniversity() != null ? user.getUniversity().getId() : null)
        .claim("studentId", user.getStudentId())
        .claim("role", user.getRole())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(accessTokenValiditySeconds)))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim("typ", "refresh")
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plusSeconds(refreshTokenValiditySeconds)))
        .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
        .compact();
  }

  public Long parseSubjectAsLong(String token) {
    var parser = Jwts.parserBuilder()
        .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
        .build();
    String sub = parser.parseClaimsJws(token).getBody().getSubject();
    return Long.valueOf(sub);
  }

}