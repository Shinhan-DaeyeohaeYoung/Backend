package com.joeun.api.ssafyAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDemandDepositAccountDepositRequest {
  @JsonProperty("Header")
  private AccountApiHeader header;

  private String accountNo;             // 입금 대상 계좌
  private String transactionBalance;    // 금액(원, 문자열)
  private String transactionSummary;    // 전표/메모
}