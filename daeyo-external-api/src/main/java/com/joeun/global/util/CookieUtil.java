package com.joeun.global.util;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {
  @Value("${app.cookie.secure:false}")    boolean secure;
  @Value("${app.cookie.same-site:Lax}")   String sameSite; // prod에서 크로스 도메인이면 None
  @Value("${app.cookie.domain:}")         String domain;

  public String buildRefreshCookie(String token, Duration maxAge) {
    ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("refresh_token", token)
        .httpOnly(true)
        .secure(secure)
        .sameSite(sameSite)
        .path("/")
        .maxAge(maxAge);

    if (domain != null && !domain.isBlank()) {
      builder.domain(domain);
    }
    return builder.build().toString();
  }
}