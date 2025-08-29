package com.joeun.api.ssafyAPI.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/** 계좌 개설 전용 헤더 (userKey 포함) — 기존 ApiHeader에는 영향 없음 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountApiHeader {
  private String apiName;
  private String transmissionDate;   // yyyyMMdd
  private String transmissionTime;   // HHmmss
  private String institutionCode;
  private String fintechAppNo;
  private String apiServiceCode;
  private String institutionTransactionUniqueNo; // 멱등키
  private String apiKey; // 관리자 키
  private String userKey; // ✅ 사용자 생성 응답으로 받은 userKey
}