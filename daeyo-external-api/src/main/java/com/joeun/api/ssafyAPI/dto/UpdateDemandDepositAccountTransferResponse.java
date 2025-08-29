package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDemandDepositAccountTransferResponse {

  @Data @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Header {
    private String responseCode;
    private String responseMessage;
    private String apiName;
    private String transmissionDate;
    private String transmissionTime;
    private String institutionCode;
    private String apiServiceCode;
    private String apiKey;
    private String institutionTransactionUniqueNo;
  }

  @JsonProperty("Header")
  private Header header;

  // 필요하면 REC 필드가 있으면 추가
}