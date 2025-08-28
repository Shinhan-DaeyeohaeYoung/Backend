package com.joeun.api.item.dto;// com.joeun.api.item.dto.ItemDtos
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ItemDtos {

    public record ItemSummaryResponse(
            Long id, Long universityId, Long organizationId,
            String name,
            Integer totalQuantity, Integer availableQuantity, Integer countWaitList,
            Boolean isActive,
            String coverKey // null 가능
    ) {}

    public record UnitPhotoSummary(String assetNo, String key) {}

    // 현재(활성) 대여 요약
    public record RentalBrief(Long rentalId, Long userId, LocalDateTime dueAt) {}

    // 유닛 페이지
    public record UnitPageResponse(
            List<UnitSummary> content,
            int page, int size, long totalElements
    ) {
        public record UnitSummary(
                Long id, Long itemId, String status, String assetNo,
                RentalBrief currentRental // 없으면 null
        ) {}
    }

    // ★ 관리자/사용자 공용 상세
    public record ItemDetailResponse(
            Long id, Long universityId, Long organizationId,
            String name, String description,
            Long deposit, Integer maxRentalDays,
            Integer totalQuantity, Integer availableQuantity, Integer countWaitList,
            Boolean isActive,
            Map<String, Long> unitStats,
            List<UnitPhotoSummary> photos,
            UnitPageResponse units // 필요 없으면 null
    ) {}
}
