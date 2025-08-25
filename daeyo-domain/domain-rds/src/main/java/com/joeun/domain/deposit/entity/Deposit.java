package com.joeun.domain.deposit.entity;

import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserBankAccount;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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
  private University university;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "organization_id",
      foreignKey = @ForeignKey(name = "fk_deposit_organization"))
  private Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
      foreignKey = @ForeignKey(name = "fk_deposit_user"))
  private User user;

  /* ===== 금액/상태 ===== */
  @Column(name = "amount", nullable = false, columnDefinition = "bigint")
  private Long amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 32)
  private DepositStatus status; // HELD / RELEASED / FORFEITED

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /* ===== 환불 계좌(환불 완료 시 기록, 선택) ===== */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_account_id",
      foreignKey = @ForeignKey(name = "fk_deposit_refund_account"))
  private UserBankAccount refundAccount;

  /* ===== 연관: Rental 쪽에서 deposit_id(FK)를 가짐 =====
     - 양방향 접근이 필요 없다면 이 컬렉션은 생략해도 됩니다.
   */
  /*
  @OneToMany(mappedBy = "deposit", fetch = FetchType.LAZY)
  private List<Rental> rentals = new ArrayList<>();
  */
}

