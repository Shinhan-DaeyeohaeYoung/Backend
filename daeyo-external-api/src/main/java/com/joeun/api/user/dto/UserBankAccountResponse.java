package com.joeun.api.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.joeun.domain.users.entity.UserBankAccount;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserBankAccountResponse {
  private Long id;
  private String bankCode;
  private String bankName;          // optional
  private String accountHolderName;
  private String accountNoMasked;   // 암호화 원문은 절대 노출 X
  private boolean primary;
  private boolean verified;
  private LocalDateTime createdAt;

  public static UserBankAccountResponse from(UserBankAccount a) {
    return UserBankAccountResponse.builder()
        .id(a.getId())
        .bankCode(a.getBankCode())
        .bankName(a.getBankName())
        .accountHolderName(a.getAccountHolderName())
        .accountNoMasked(a.getAccountNoMasked())
        .primary(a.isPrimary())
        .verified(a.isVerified())
        .createdAt(a.getCreatedAt())
        .build();
  }
}
