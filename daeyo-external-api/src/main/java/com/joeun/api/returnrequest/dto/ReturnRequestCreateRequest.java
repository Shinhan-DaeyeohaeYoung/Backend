package com.joeun.api.returnrequest.dto;

import java.time.LocalDateTime;

public record ReturnRequestCreateRequest(
        Long universityId,
        Long organizationId,
        Long userId,
        Long rentalId,
        String imageKey,
        String imageMime,
        String imageHash,
        LocalDateTime imageTakenAt // 없으면 서비스에서 now()로 대체 가능
) {}

