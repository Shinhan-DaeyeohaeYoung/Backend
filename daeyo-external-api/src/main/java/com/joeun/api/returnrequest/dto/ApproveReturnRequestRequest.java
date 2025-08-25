package com.joeun.api.returnrequest.dto;

public record ApproveReturnRequestRequest(
        Long universityId,
        Long organizationId,
        Long approverUserId
) {}