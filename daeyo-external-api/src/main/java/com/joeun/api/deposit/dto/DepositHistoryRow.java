package com.joeun.api.deposit.dto;

public record DepositHistoryRow(
    Long eventId,
    java.math.BigDecimal amount,
    String eventType,              // CREATED / REFUNDED / FORFEITED
    java.time.LocalDateTime occurredAt,
    String organizationName
) {}
