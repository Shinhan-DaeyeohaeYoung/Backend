package com.joeun.api.organization.dto;

import com.joeun.domain.organization.types.OrganizationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationResponse {

  private Long organizationId;
  private Long universityId;
  private String name;
  private OrganizationType type;
  private Long parentOrganizationId;
  private boolean isActive;
  private String role; // ORG_ADMIN 고정 반환
  private boolean isInherited; // 상속 여부 (false 고정 반환)
}