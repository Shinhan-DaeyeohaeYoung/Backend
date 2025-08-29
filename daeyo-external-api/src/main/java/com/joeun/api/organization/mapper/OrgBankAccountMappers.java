package com.joeun.api.organization.mapper;

import com.joeun.api.organization.dto.OrgBankAccountResponse;
import com.joeun.domain.organization.entity.OrgBankAccount;
import java.math.BigDecimal;

public final class OrgBankAccountMappers {
  private OrgBankAccountMappers() {}

  public static OrgBankAccountResponse toResponse(OrgBankAccount a) {
    return new OrgBankAccountResponse(
        a.getId(),
        a.getBankCode(),
        a.getBankName(),
        a.getAccountHolderName(),
        a.getAccountNo(),   // ✅ 평문
        a.isPrimary(),
        a.isVerified(),
        null
    );
  }

  public static OrgBankAccountResponse toResponse(OrgBankAccount a, BigDecimal balance) {
    return new OrgBankAccountResponse(
        a.getId(),
        a.getBankCode(),
        a.getBankName(),
        a.getAccountHolderName(),
        a.getAccountNo(),   // ✅ 평문
        a.isPrimary(),
        a.isVerified(),
        balance
    );
  }
}
