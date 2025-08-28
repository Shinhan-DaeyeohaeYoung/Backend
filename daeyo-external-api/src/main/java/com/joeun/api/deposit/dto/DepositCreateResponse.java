package com.joeun.api.deposit.dto;

import com.joeun.domain.deposit.types.DepositStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class DepositCreateResponse {
  private Long id;
  private Long userId;
  private Long universityId;
  private Long organizationId;
  private BigDecimal amount;
  private DepositStatus status;     // HELD
  private Long refundAccountId;     // nullable
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private Long orgBankAccountId;
}