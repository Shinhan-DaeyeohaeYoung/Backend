package com.joeun.api.deposit.dto;

import com.joeun.domain.deposit.entity.Deposit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class DepositResponse {

  private Long id;
  private BigDecimal amount;
  private String status;
  private LocalDateTime created_at;
  private LocalDateTime updated_at;
  private Long refund_account_id; // nullable

  public static DepositResponse from(Deposit d) {
    return new DepositResponse(
        d.getId(),
        d.getAmount(),
        d.getStatus().name(),
        d.getCreatedAt(),
        d.getUpdatedAt(),
        d.getRefundAccount() == null ? null : d.getRefundAccount().getId()
    );
  }
}