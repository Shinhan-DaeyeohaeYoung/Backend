package com.joeun.domain.organization.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "organization_bank_account",
    indexes = {
        @Index(name = "idx_org_account_org", columnList = "organization_id"),
        @Index(name = "idx_org_account_is_primary", columnList = "is_primary")
    }
)
@Data
@NoArgsConstructor
public class OrgBankAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  @Column(name = "organization_id", nullable = false, columnDefinition = "bigint")
  private Long organizationId;  // 단방향 숫자 FK(간단)

  @Column(name = "bank_code", length = 10)
  private String bankCode;

  @Column(name = "bank_name", length = 64)
  private String bankName;

  @Column(name = "account_holder_name", length = 100, nullable = false)
  private String accountHolderName;

  @Column(name = "account_no_masked", length = 32)
  private String accountNoMasked;

  @Column(name = "is_primary", nullable = false, columnDefinition = "boolean default false")
  private boolean isPrimary;

  @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
  private boolean isVerified;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}