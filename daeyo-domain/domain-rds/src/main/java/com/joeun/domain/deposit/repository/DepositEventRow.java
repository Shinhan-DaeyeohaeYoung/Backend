package com.joeun.domain.deposit.repository;

import com.joeun.domain.deposit.types.DepositEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DepositEventRow {
  Long getEventId();
  BigDecimal getAmount();
  DepositEventType getEventType();
  LocalDateTime getOccurredAt();
  String getOrganizationName();
}