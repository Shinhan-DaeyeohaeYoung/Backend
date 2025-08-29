package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateDemandDepositAccountTransferRequest {

  @JsonProperty("Header")
  private AccountApiHeader header;

  // 입금(수취) 계좌
  private String depositAccountNo;
  private String depositTransactionSummary;

  // 이체 금액 (문자열)
  private String transactionBalance;

  // 출금(송금) 계좌
  private String withdrawalAccountNo;
  private String withdrawalTransactionSummary;
}
