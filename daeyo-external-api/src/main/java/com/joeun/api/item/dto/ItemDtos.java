package com.joeun.api.item.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ItemDtos {
    /* 요청 */
    public record ItemCreateRequest(
            Long universityId,
            Long organizationId,
            String name,
            String description,
            Long deposit,
            Integer maxRentalDays,
            Boolean isActive
    ) {}
    public record ItemPatchRequest(
            String name,
            String description,
            Long deposit,
            Integer maxRentalDays,
            Boolean isActive
    ) {}
    public record UnitBatchCreateRequest(List<UnitCreate> units) {
        public record UnitCreate(String assetNo, String description, String status) {}
    }

    /* 응답 */
    public record ItemSummaryResponse(
            Long id,
            Long universityId,
            Long organizationId,
            String name,
            Integer totalQuantity,
            Integer availableQuantity,
            Boolean isActive,
            String coverPhotoKey
    ) {}
    public record UnitPhotoSummary(String assetNo, String key) {}

    public record ItemDetailResponse(
            Long id,
            Long universityId,
            Long organizationId,
            String name,
            String description,
            Long deposit,
            Integer maxRentalDays,
            Integer totalQuantity,
            Integer availableQuantity,
            Boolean isActive,
            Map<String, Long> unitStats,
            List<UnitPhotoSummary> photos
    ) {}
    public record UnitPageResponse(List<UnitSummary> content, int page, int size, long totalElements) {
        public record UnitSummary(Long id, Long itemId, String status, String assetNo) {}
    }
    // ★ 유닛 상세(사진/대여정보 포함)
    public record UnitDetail(
            Long id,
            String assetNo,
            String status,
            String description,
            String photoKey,        // 해당 유닛의 대표 사진 키(없으면 null)
            RentalBrief rental      // 대여중이면 정보, 아니면 null
    ) {}
    public record RentalBrief(
            Long rentalId,
            Long userId,
            LocalDateTime rentedAt,
            LocalDateTime dueAt,
            String status
            // 필요하면 userName, email 등을 추가(추후 User 조회 연동 시)
    ) {}
}
