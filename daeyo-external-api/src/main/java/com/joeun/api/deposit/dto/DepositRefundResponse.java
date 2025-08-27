package com.joeun.api.deposit.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class DepositRefundResponse {
  Long id;
  String status;          // RELEASED
  LocalDateTime updatedAt;
}