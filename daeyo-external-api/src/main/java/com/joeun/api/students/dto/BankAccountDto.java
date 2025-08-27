package com.joeun.api.students.dto;

public record BankAccountDto(
    String accountNoMasked,
    String bankCode,
    String bankName,
    boolean isPrimary,
    boolean isVerified
) {}