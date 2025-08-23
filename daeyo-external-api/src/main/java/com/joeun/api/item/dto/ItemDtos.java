package com.joeun.api.item.dto;

import java.util.List;
import java.util.Map;

public final class ItemDtos {
    /* 요청 */
    public record ItemCreateRequest(
            Long universityId, Long organizationId,
            String name, String description, Long deposit, Integer maxRentalDays, Boolean isActive
    ) {}
    public record ItemPatchRequest(
            String name, String description, Long deposit, Integer maxRentalDays, Boolean isActive
    ) {}
    public record UnitBatchCreateRequest(List<UnitCreate> units) {
        public record UnitCreate(String assetNo, String description, String status) {}
    }

    /* 응답 */
    public record ItemSummaryResponse(
            Long id, Long universityId, Long organizationId, String name,
            Integer totalQuantity,Integer availableQuantity, Boolean isActive
    ) {}
    public record ItemDetailResponse(
            Long id, Long universityId, Long organizationId, String name, String description,
            Long deposit, Integer maxRentalDays, Integer totalQuantity, Integer availableQuantity,
            Boolean isActive, Map<String, Long> unitStats
    ) {}
    public record UnitPageResponse(List<UnitSummary> content, int page, int size, long totalElements) {
        public record UnitSummary(Long id, Long itemId, String status, String assetNo) {}
    }
}
