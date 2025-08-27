package com.joeun.api.students.dto;

import java.util.List;

public record StudentSignupResponse(
    UserDto user,
    List<UserMembershipDto> memberships,
    BankAccountDto bankAccount   // ★ 새 필드 추가
) {
  public static record UserDto(
      Long id,
      Long universityId,
      String name,
      String email,
      String studentId,
      String role
  ) {}

  public static record BankAccountDto(
      String accountNoMasked,
      String bankCode,
      String bankName,
      boolean isPrimary,
      boolean isVerified
  ) {}
}