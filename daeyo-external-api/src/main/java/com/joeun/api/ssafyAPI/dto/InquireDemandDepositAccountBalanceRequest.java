package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InquireDemandDepositAccountBalanceRequest {

  @JsonProperty("Header")
  private AccountApiHeader header;     // ← 기존에 만든 userKey 포함 헤더 재사용

  private String accountNo;            // 평문 계좌번호 (외부 API 요구 포맷)
}