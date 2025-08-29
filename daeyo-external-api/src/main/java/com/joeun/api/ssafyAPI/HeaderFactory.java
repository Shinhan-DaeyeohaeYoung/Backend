package com.joeun.api.ssafyAPI;

import com.joeun.api.ssafyAPI.dto.ApiHeader;
import com.joeun.api.user.service.UserService;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HeaderFactory {

  @Value("${ssafy.institution-code}")
  private String institutionCode;

  @Value("${ssafy.fintech-app-no}")
  private String fintechAppNo;

  @Value("${ssafy.api-key}")
  private String adminApiKey;

  private final UserService credentialAppService;

  public ApiHeader adminHeader(String apiName, String serviceCode) {
    var now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    return new ApiHeader(
        apiName,
        DateTimeFormatter.ofPattern("yyyyMMdd").format(now),
        DateTimeFormatter.ofPattern("HHmmss").format(now),
        institutionCode,
        fintechAppNo,
        serviceCode,
        idemKey(apiName),
        adminApiKey
    );
  }

  public ApiHeader userHeader(Long executorUserId, String apiName, String serviceCode) {
    String userKey = credentialAppService.getUserKey(executorUserId);
    var h = adminHeader(apiName, serviceCode);
    h.setApiKey(userKey);
    return h;
  }

  private String idemKey(String api) {
    var now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now)
        + "-" + api + "-" + UUID.randomUUID().toString().substring(0, 8);
  }
}