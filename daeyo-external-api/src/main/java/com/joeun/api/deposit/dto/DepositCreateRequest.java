package com.joeun.api.deposit.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class DepositCreateRequest {
  @NotNull
  private Long user_id;

  @NotNull
  private Long organization_id;

  @NotNull
  @DecimalMin("0.01")
  private BigDecimal amount;

  private Long university_id; // optional (보내면 조직 대학과 일치해야 함)
}