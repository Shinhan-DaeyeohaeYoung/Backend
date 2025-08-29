package com.joeun.api.organization.dto;

import lombok.Value;
import java.math.BigDecimal;

@Value
public class OrgBankAccountResponse {
  Long id;
  String bankCode;
  String bankName;
  String accountHolderName;
  String accountNo;       // ✅ 평문
  boolean isPrimary;
  boolean isVerified;
  BigDecimal accountBalance; // 잔액 포함 사용 시
}