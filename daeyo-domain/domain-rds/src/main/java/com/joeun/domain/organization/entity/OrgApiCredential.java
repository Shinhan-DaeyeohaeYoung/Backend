package com.joeun.domain.organization.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "org_api_credentials",
    indexes = {
        @Index(name = "idx_orgcred_org", columnList = "organization_id")
    }
)
@Getter @Setter
public class OrgApiCredential {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_orgcred_org"))
  private Organization organization;

  @Column(name = "user_key", nullable = false, length = 128)
  private String userKey; // SSAFY에서 조직 계좌 개설 시 발급받은 userKey

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}