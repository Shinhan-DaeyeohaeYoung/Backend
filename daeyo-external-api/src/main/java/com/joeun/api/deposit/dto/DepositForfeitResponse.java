package com.joeun.api.deposit.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class DepositForfeitResponse {
  Long id;
  String status;          // FORFEITED
  LocalDateTime updatedAt;
}