package com.joeun.api.university.dto;

import com.joeun.domain.university.entity.University;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UniversityResponse {
  private Long id;
  private String name;
  private String code;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static UniversityResponse from(University u) {
    return UniversityResponse.builder()
        .id(u.getId())
        .name(u.getName())
        .code(u.getCode())
        .createdAt(u.getCreatedAt())
        .updatedAt(u.getUpdatedAt())
        .build();
  }
}