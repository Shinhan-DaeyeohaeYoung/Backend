package com.joeun.domain.users.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "user_bank_account",
    indexes = {
        @Index(name = "idx_account_user", columnList = "user_id"),
        @Index(name = "idx_account_is_primary", columnList = "is_primary")
    }
)
@Data
@NoArgsConstructor
public class UserBankAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  /* ===== 소속 사용자 ===== */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "user_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_account_user")
  )
  private User user;

  /* ===== 계좌 정보 ===== */
  @Column(name = "bank_code", length = 10)
  private String bankCode;   // 은행 코드(예: 088)

  @Column(name = "bank_name", length = 64)
  private String bankName;   // 은행명(옵션)

  @Column(name = "account_holder_name", length = 100, nullable = false)
  private String accountHolderName;  // 예금주명

  @Column(name = "account_no_masked", length = 32)
  private String accountNoMasked;    // ****1234 표시용

  @Lob
  @Column(name = "account_no", nullable = false)
  private byte[] accountNo;          // 암호화/토큰화 저장(평문 금지)

  @Column(name = "is_primary", nullable = false, columnDefinition = "boolean default false")
  private boolean isPrimary = false; // 대표 계좌 여부

  @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
  private boolean isVerified = false; // 1원 인증 등 검증 여부

  /* ===== 생성/수정 시간 ===== */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}