package com.joeun.api.organization.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrganizationJoinRequest {
  @NotNull
  private Long organizationId;
}
