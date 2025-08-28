package com.joeun.service.deposit;

import com.joeun.domain.deposit.types.DepositEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrgDepositEventView(
    Long eventId,
    BigDecimal amount,
    DepositEventType eventType,
    LocalDateTime occurredAt,
    String userName
) {}