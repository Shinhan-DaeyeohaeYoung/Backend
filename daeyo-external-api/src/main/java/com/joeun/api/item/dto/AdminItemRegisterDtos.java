package com.joeun.api.item.dto;

import java.math.BigDecimal;
import java.util.List;

public final class AdminItemRegisterDtos {
    public record RegisterRequest(
        Long universityId, Long organizationId,
        String name, String description, BigDecimal deposit, Integer maxRentalDays,
        List<UnitCreate> units
    ) {}

    public record ItemCreateRequest(
            Long universityId, Long organizationId,
            String name, String description, BigDecimal deposit, Integer maxRentalDays,
            Boolean isActive
    ) {}

    public record ItemPatchRequest(
            String name, String description, BigDecimal deposit, Integer maxRentalDays,
            Boolean isActive
    ) {}

    public record UnitBatchCreateRequest(List<UnitCreate> units) {}

    public record UnitCreate(String assetNo, String description, String status, Photo photo) {}

    // 사진 메타(프론트가 S3에 올린 후 key만 내려줌)
    public record Photo(String key, String mime, String hash, String takenAt) {}

    public record RegisterResponse(Long itemId, int unitsCreated) {}
}
