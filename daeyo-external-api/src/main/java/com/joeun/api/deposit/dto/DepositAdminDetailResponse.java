package com.joeun.api.deposit.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class DepositAdminDetailResponse {
  Long id;
  Long universityId;
  Long organizationId;
  Long userId;
  BigDecimal amount;
  String status; // enum name
  LocalDateTime createdAt;
  LocalDateTime updatedAt;
  Long refundAccountId;
}