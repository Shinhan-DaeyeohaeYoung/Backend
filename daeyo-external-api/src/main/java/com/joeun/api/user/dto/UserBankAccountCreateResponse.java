package com.joeun.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserBankAccountCreateResponse {
  private final Long id;
  private final String accountNoMasked;
  private final boolean primary;
  private final boolean verified;
}
