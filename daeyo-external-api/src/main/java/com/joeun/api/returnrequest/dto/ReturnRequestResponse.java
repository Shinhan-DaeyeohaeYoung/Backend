package com.joeun.api.returnrequest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 응답에서 생략
public record ReturnRequestResponse(
        Long id,
        Long universityId,
        Long organizationId,
        Long rentalId,
        Long userId,
        Long approverUserId,
        ReturnRequestStatus status,
        String submittedImageKey,
        String submittedImageUrl,   //  after URL
        // (옵션) 관리자 상세에서 함께 보여줄 "기준 사진"
        String beforeImageKey,      // ← 필요 없으면 null
        String beforeImageUrl,      // ← 필요 없으면 null
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        Boolean isActive
) {
    /**  기존 코드 호환: URL 없이도 만들 수 있게 오버로드 유지 */
    public static ReturnRequestResponse from(ReturnRequest rr) {
        return from(rr, null, null, null);
    }

    /**  after URL만 넣는 현재 형태 */
    public static ReturnRequestResponse from(ReturnRequest rr, String submittedImageUrl) {
        return from(rr, submittedImageUrl, null, null);
    }

    /**  (옵션) before/after 모두 지원 */
    public static ReturnRequestResponse from(
            ReturnRequest rr,
            String submittedImageUrl,
            String beforeImageKey,
            String beforeImageUrl
    ) {
        return new ReturnRequestResponse(
                rr.getId(),
                rr.getUniversityId(),
                rr.getOrganizationId(),
                rr.getRental() != null ? rr.getRental().getId() : null,
                rr.getUserId(),
                rr.getApproverUserId(),
                rr.getStatus(),
                rr.getSubmittedImageKey(),
                submittedImageUrl,
                beforeImageKey,
                beforeImageUrl,
                rr.getRequestedAt(),
                rr.getApprovedAt(),
                rr.getRejectedAt(),
                rr.getIsActive()
        );
    }
}
