package com.joeun.service.deposit;

import com.joeun.domain.deposit.types.DepositEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 조회 전용 read model (엔티티 없음) */
public record DepositEventView(
    Long eventId,
    BigDecimal amount,
    DepositEventType eventType,
    LocalDateTime occurredAt,
    String organizationName
) {}