package com.joeun.api.university.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UniversityPointResponse {
  private Long university_id;
  private Long point;        // 정수 포인트 (필요 시 BigDecimal로 교체)
  private String created_at;
  private String updated_at; // ISO 8601 문자열 응답
}