package com.joeun.api.organization.dto;


import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.types.UserOrgRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserOrgMembershipResponse {
  private Long userId;
  private Long organizationId;
  private String role;
  private LocalDateTime createdAt;

  public static UserOrgMembershipResponse from(UserOrgMembership m) {
    return UserOrgMembershipResponse.builder()
        .userId(m.getId().getUserId())              // EmbeddedId에서 바로 꺼냄
        .organizationId(m.getId().getOrganizationId())
        .role(m.getRole().name())
        .createdAt(m.getCreatedAt())
        .build();
  }
}