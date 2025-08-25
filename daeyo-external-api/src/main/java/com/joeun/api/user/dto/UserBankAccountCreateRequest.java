package com.joeun.api.user.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBankAccountCreateRequest {

  // 둘 중 하나는 필수 (아래 @AssertTrue에서 검증)
  private String bankCode;                 // 예: "088"
  private String bankName;                 // 예: "신한은행"

  @NotBlank
  private String accountHolderName;

  @NotBlank
  @Pattern(regexp = "^\\d{6,30}$", message = "계좌번호는 숫자만 6~30자리로 입력")
  private String accountNo;                // 숫자만

  @AssertTrue(message = "bankCode 또는 bankName 중 하나는 반드시 입력하세요")
  public boolean hasBankInfo() {
    return (bankCode != null && !bankCode.isBlank())
        || (bankName != null && !bankName.isBlank());
  }
}