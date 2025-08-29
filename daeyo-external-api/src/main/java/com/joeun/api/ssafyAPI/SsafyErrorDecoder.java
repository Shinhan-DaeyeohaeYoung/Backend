package com.joeun.api.ssafyAPI;


import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SsafyErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    try {
      // SSAFY API는 JSON Body에 Header.responseCode, Header.responseMessage 있음
      // Body 파싱은 필요 시 ObjectMapper 사용
      String body = response.body() != null
          ? response.body().toString()
          : "no-body";

      log.error("SSAFY API Error: method={}, status={}, body={}",
          methodKey, response.status(), body);

      // 상태 코드에 따라 커스텀 예외 분기
      if (response.status() == 400) {
        return new SsafyApiException("INVALID_REQUEST", "잘못된 요청", response);
      } else if (response.status() == 401 || response.status() == 403) {
        return new SsafyApiException("UNAUTHORIZED", "인증 실패", response);
      } else if (response.status() >= 500) {
        return new SsafyApiException("SERVER_ERROR", "SSAFY 서버 오류", response);
      }

      // 그 외는 기본 디코더 위임
      return defaultDecoder.decode(methodKey, response);

    } catch (Exception e) {
      return new SsafyApiException("DECODER_ERROR", "에러 디코딩 실패", response, e);
    }
  }
}