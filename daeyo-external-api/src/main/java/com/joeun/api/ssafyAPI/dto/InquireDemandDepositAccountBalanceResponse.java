package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InquireDemandDepositAccountBalanceResponse {

  @Data @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Header {
    private String responseCode;
    private String responseMessage;
    private String apiName;
    private String transmissionDate;
    private String transmissionTime;
    private String institutionCode;
    private String apiKey;
    private String apiServiceCode;
    private String institutionTransactionUniqueNo;
  }

  @Data @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Rec {
    private String bankCode;
    private String accountNo;
    private String accountBalance;        // 외부는 문자열로 줌
    private String accountCreatedDate;
    private String accountExpiryDate;
    private String lastTransactionDate;
    private String currency;
  }

  @JsonProperty("Header")
  private Header header;

  @JsonProperty("REC")
  private Rec rec;
}