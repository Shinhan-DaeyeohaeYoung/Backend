package com.joeun.domain.deposit.entity;

import com.joeun.domain.deposit.types.DepositEventType;
import com.joeun.domain.deposit.types.DepositStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "deposit_event")
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class DepositEvent {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deposit_id", nullable = false)
  private Deposit deposit;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "organization_id")
  private Long organizationId;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", length = 20, nullable = false)
  private DepositEventType eventType;

  @Column(name = "occurred_at", nullable = false)
  private LocalDateTime occurredAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "prev_status", length = 20)
  private DepositStatus prevStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", length = 20)
  private DepositStatus newStatus;

  @Column(length = 255)
  private String note;
}
