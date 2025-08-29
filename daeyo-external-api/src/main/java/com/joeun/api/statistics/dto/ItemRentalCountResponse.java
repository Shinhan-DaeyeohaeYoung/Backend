package com.joeun.api.statistics.dto;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import lombok.Builder;

public record ItemRentalCountResponse (
    Long itemId,
    String itemName,
    Long rentalCount
) {
    @Builder
    public ItemRentalCountResponse {
    }

    public static ItemRentalCountResponse from (DailyItemRentalCount dailyItemRentalCount) {
        return ItemRentalCountResponse.builder()
                .itemId(dailyItemRentalCount.getItemId())
                .itemName(dailyItemRentalCount.getItemName())
                .rentalCount(dailyItemRentalCount.getRentalCount().longValue())
                .build();
    }

}
