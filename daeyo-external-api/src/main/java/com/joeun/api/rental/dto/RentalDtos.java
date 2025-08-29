package com.joeun.api.rental.dto;

import com.joeun.api.item.dto.ItemDtos;

import java.time.LocalDateTime;
import java.util.List;

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
            String reserveExpiresAt,
            String unitImageUrl
    ) {}

    public record ApproveResponse(Long id, String status, String dueAt) {}
    public static record RentalHistoryItem(
            Long id,
            String status,          // RESERVED / RENTED / CANCELLED / RETURNED ...
            Long itemId,
            Long unitId,
            String reservedAt,
            String reserveExpiresAt,
            String rentedAt,
            String dueAt,
            String returnedAt,
            boolean expired,         // RESERVED였으나 TTL 만료되었으면 true
            String unitImageUrl
    ) {}
    public static record CurrentRentalItem(
            Long id,
            Long universityId,
            Long organizationId,
            Long userId,
            Long itemId,
            Long individualItemId,
            Integer quantity,
            String rentedAt,
            String dueAt,
            String returnedAt,
            String status,
            Long depositId,
            String unitImageUrl
    ) {}
    public record UnitReservationDetail(
            Long rentalId,
            Long unitId,
            String assetNo,
            String unitStatus,
            Long itemId,
            String description,
            Long universityId,
            Long organizationId,
            List<ItemDtos.UnitPhotoSummary> photos
    ) {}



}

