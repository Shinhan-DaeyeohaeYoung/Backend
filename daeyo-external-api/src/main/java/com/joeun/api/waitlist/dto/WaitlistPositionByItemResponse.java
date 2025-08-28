package com.joeun.api.waitlist.dto;

import lombok.Builder;

public record WaitlistPositionByItemResponse(
        int myPosition,
        int totalCount
) {
    @Builder
    public WaitlistPositionByItemResponse {
    }
}
