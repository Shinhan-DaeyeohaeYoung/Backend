package com.joeun.api.deposit.dto;

import com.joeun.domain.deposit.types.DepositStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DepositListDto {
  private Long id;
  private BigDecimal amount;
  private DepositStatus status;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long refundAccountId; // null 가능
}