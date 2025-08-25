package com.joeun.api.rental.dto;

import java.time.LocalDateTime;

public final class RentalDtos {
    public record ReserveRequest(Long itemId, Long unitId, Integer ttlMinutes) {}
    public record ReserveResponse(Long rentalId, Long itemId, Long unitId,
                                  LocalDateTime reservedAt, LocalDateTime reserveExpiresAt, String status) {}
    public record ReservationSummary(
            Long id,
            Long itemId,
            Long unitId,
            String status,
            String reservedAt,
            String reserveExpiresAt
    ) {}

    public record ApproveResponse(Long id, String status, String dueAt) {}
}

