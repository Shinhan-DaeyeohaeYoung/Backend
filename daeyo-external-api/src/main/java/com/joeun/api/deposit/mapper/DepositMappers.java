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
        .orgBankAccountId(d.getOrgBankAccount() != null ? d.getOrgBankAccount().getId() : null) // ★ 추가
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .build();
  }

}