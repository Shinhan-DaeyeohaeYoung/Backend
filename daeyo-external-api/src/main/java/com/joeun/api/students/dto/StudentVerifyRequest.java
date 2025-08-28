package com.joeun.api.students.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StudentVerifyRequest(
    @NotNull Long universityId,
    @NotBlank String studentNo,
    @NotBlank String password
) {}