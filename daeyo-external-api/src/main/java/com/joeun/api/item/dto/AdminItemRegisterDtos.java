package com.joeun.api.item.dto;

import java.util.List;

public final class AdminItemRegisterDtos {
    public record RegisterRequest(
            Long universityId, Long organizationId,
            String name, String description, Long deposit, Integer maxRentalDays,
            List<UnitCreate> units
    ) {}
    public record UnitCreate(String assetNo, String description, String status) {}
    public record RegisterResponse(Long itemId, int unitsCreated) {}
}
