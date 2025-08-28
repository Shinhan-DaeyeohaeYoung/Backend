package com.joeun.api.returnrequest.dto;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import java.time.LocalDateTime;

public record ReturnRequestResponse(
        Long id,
        Long universityId,
        Long organizationId,
        Long rentalId,
        Long userId,
        Long approverUserId,
        ReturnRequestStatus status,
        String submittedImageKey,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        Boolean isActive
) {
    public static ReturnRequestResponse from(ReturnRequest rr) {
        return new ReturnRequestResponse(
                rr.getId(),
                rr.getUniversityId(),
                rr.getOrganizationId(),
                rr.getRental().getId(),
                rr.getUserId(),
                rr.getApproverUserId(),
                rr.getStatus(),
                rr.getSubmittedImageKey(),
                rr.getRequestedAt(),
                rr.getApprovedAt(),
                rr.getRejectedAt(),
                rr.getIsActive()
        );
    }
}
