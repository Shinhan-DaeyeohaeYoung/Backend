package com.joeun.api.ssafyAPI;

import feign.Response;

public class SsafyApiException extends RuntimeException {
  private final String code;
  private final Response response;

  public SsafyApiException(String code, String message, Response response) {
    super(message);
    this.code = code;
    this.response = response;
  }

  public SsafyApiException(String code, String message, Response response, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.response = response;
  }

  public String getCode() { return code; }
  public Response getResponse() { return response; }
}