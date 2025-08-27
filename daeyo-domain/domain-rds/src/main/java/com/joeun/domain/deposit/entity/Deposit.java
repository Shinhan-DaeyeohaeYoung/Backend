package com.joeun.domain.deposit.entity;

import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserBankAccount;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "deposit",
    indexes = {
        @Index(name = "idx_deposit_univ_user_status", columnList = "university_id, user_id, status")
    }
)
@Data
@NoArgsConstructor
public class Deposit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "bigint")
  private Long id;

  /* ===== 소속 대학/조직/사용자 ===== */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "university_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_deposit_university"))
  @ToString.Exclude @EqualsAndHashCode.Exclude
  private University university;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id",
      foreignKey = @ForeignKey(name = "fk_deposit_organization"))
  @ToString.Exclude @EqualsAndHashCode.Exclude
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_deposit_user"))
  @ToString.Exclude @EqualsAndHashCode.Exclude
  private User user;

  /* ===== 금액/상태 ===== */
  @Column(name = "amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private DepositStatus status; // HELD / RELEASED / FORFEITED

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /* ===== 환불 계좌(환불 완료 시 기록, 선택) ===== */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_account_id",
      foreignKey = @ForeignKey(name = "fk_deposit_refund_account"))
  @ToString.Exclude @EqualsAndHashCode.Exclude
  private UserBankAccount refundAccount;

  /* ===== 연관: Rental 쪽에서 deposit_id(FK)를 가짐 ===== */
//  @OneToMany(mappedBy = "deposit", fetch = FetchType.LAZY)
//  @ToString.Exclude @EqualsAndHashCode.Exclude
//  private List<Rental> rentals = new ArrayList<>();
}
