package com.joeun.api.students.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StudentSignupRequest(
    @NotNull Long universityId,
    @NotBlank String studentNo,
    @NotBlank String password
    /*@NotBlank
    String bankCode,           // 예: "088"

    @Size(max = 64)
    String bankName,           // 옵션

    @NotBlank
    String accountHolderName,

    @NotBlank
    @Pattern(regexp = "^[0-9]{6,30}$", message = "계좌번호는 숫자만 6~30자리로 입력")
    String accountNo*/
) {}