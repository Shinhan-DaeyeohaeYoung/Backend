package com.joeun.api.ssafyAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiHeader {
  private String apiName;
  private String transmissionDate;   // yyyyMMdd
  private String transmissionTime;   // HHmmss
  private String institutionCode;
  private String fintechAppNo;
  private String apiServiceCode;
  private String institutionTransactionUniqueNo; // 멱등키
  private String apiKey; // 기본: 관리자 키 사용
}