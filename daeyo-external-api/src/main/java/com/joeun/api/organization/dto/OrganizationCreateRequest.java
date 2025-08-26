package com.joeun.api.organization.dto;

import com.joeun.domain.organization.types.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreateRequest {

  @NotNull
  private Long universityId;

  @NotNull
  private Long parentOrganizationId;

  @NotBlank
  private String name;

  @NotNull
  private OrganizationType type;
}