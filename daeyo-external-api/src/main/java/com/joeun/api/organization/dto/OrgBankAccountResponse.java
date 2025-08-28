package com.joeun.api.organization.dto;

import lombok.Value;

@Value
public class OrgBankAccountResponse {
  Long id;
  String bankCode;
  String bankName;
  String accountHolderName;
  String accountNoMasked;
  boolean isPrimary;
  boolean isVerified;
}