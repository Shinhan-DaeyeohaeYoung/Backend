package com.joeun.api.organization.mapper;

import com.joeun.api.organization.dto.OrgBankAccountResponse;
import com.joeun.domain.organization.entity.OrgBankAccount;

public final class OrgBankAccountMappers {
  private OrgBankAccountMappers() {}

  public static OrgBankAccountResponse toResponse(OrgBankAccount a) {
    return new OrgBankAccountResponse(
        a.getId(),
        a.getBankCode(),
        a.getBankName(),
        a.getAccountHolderName(),
        a.getAccountNoMasked(),
        a.isPrimary(),
        a.isVerified()
    );
  }
}