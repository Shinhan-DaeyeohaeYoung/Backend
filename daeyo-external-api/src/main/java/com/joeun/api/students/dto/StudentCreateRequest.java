package com.joeun.api.students.dto;

import jakarta.validation.constraints.*;

public record StudentCreateRequest(
    @NotNull Long universityId,
    @NotBlank String studentNo,
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String password
) {}