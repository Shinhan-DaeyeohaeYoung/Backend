package com.joeun.api.deposit.mapper;

import com.joeun.api.deposit.dto.DepositCreateResponse;
import com.joeun.api.deposit.dto.DepositListDto;
import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.rental.entity.Rental;

public final class DepositMappers {
  private DepositMappers() {}

  public static DepositListDto toListDto(Deposit d) {
    return DepositListDto.builder()
        .id(d.getId())
        .amount(d.getAmount())
        .status(d.getStatus())
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .refundAccountId(
            d.getRefundAccount() != null ? d.getRefundAccount().getId() : null
        )
        .userName(
            d.getUser() != null ? d.getUser().getName() : null
        )
        .build();
  }

  // 신규: 생성 응답 매핑(도메인 엔티티만 입력 받음)
  public static DepositCreateResponse toCreateResponse(Deposit d) {
    if (d == null) return null;
    return DepositCreateResponse.builder()
        .id(d.getId())
        .userId(d.getUser() != null ? d.getUser().getId() : null)
        .universityId(d.getUniversity() != null ? d.getUniversity().getId() : null)
        .organizationId(d.getOrganization() != null ? d.getOrganization().getId() : null)
        .amount(d.getAmount())
        .status(d.getStatus())
        .refundAccountId(d.getRefundAccount() != null ? d.getRefundAccount().getId() : null)
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .build();
  }

}