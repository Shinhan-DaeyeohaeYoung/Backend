package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateDemandDepositAccountResponse {

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
  public static class Currency {
    private String currency;
    private String currencyName;
  }

  @Data @NoArgsConstructor @AllArgsConstructor @Builder
  public static class Rec {
    private String bankCode;
    private String accountNo;
    private Currency currency;
  }

  @JsonProperty("Header")
  private Header header;

  @JsonProperty("REC")
  private Rec rec;
}