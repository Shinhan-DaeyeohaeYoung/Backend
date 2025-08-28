package com.joeun.api.organization.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MyOrganizationResponse {
  private Long organizationId;
  private Long universityId;
  private String name;
  private String type;
  private Long parentOrganizationId;
  private boolean isActive;
  private String role;
}