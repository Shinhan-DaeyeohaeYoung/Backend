package com.joeun.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSigninRequest {
  @NotNull(message = "universityId is required")
  @Positive(message = "universityId must be a positive number")
  private Long universityId;

  @NotBlank
  private String studentId;

  @NotBlank @Size(min = 8, max = 64)
  private String password;
}
