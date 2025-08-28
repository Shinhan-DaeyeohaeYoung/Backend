package com.joeun.domain.users.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class UserOrgMembershipId implements Serializable {
  private Long userId;
  private Long organizationId;

  public UserOrgMembershipId(Long userId, Long organizationId) {
    this.userId = userId;
    this.organizationId = organizationId;
  }
}