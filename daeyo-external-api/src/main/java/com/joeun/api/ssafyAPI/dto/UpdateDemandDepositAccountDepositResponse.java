package com.joeun.api.ssafyAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDemandDepositAccountDepositResponse {
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
}