package com.joeun.domain.users.entity;

import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.users.types.UserOrgRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_org_membership",
    indexes = {
        @Index(name = "idx_membership_user_org", columnList = "user_id, organization_id", unique = true)
    }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserOrgMembership {

  @EmbeddedId
  @Builder.Default
  private UserOrgMembershipId id = new UserOrgMembershipId();

  @MapsId("userId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_membership_user"))
  private User user;

  @MapsId("organizationId")
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_membership_organization"))
  private Organization organization;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 32)
  private UserOrgRole role; // ORG_ADMIN / ORG_MEMBER

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /* === 편의 생성자 === */
  public static UserOrgMembership of(User user, Organization org, UserOrgRole role) {
    UserOrgMembership m = new UserOrgMembership();
    m.user = user;
    m.organization = org;
    m.role = role;
    m.id = new UserOrgMembershipId(user.getId(), org.getId());
    return m;
  }
}