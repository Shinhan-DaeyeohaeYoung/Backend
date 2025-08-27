package com.joeun.api.university.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UniversityPointTopResponse {
  private List<UniversityPointTopItem> items;
  private int count;
}