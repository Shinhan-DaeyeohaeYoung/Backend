package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 계좌 개설 요청 DTO — Header 타입만 AccountApiHeader로 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateDemandDepositAccountRequest {

  @JsonProperty("Header")
  private AccountApiHeader header;

  /** 상품 고유번호 (예: "001-1-ffa4253081d540") */
  private String accountTypeUniqueNo;
}