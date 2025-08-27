package com.joeun.api.university.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UniversityPointTopItem {
  private Long university_id;
  private String name;
  private String code;
  private Long point;
  private String updated_at;
}
