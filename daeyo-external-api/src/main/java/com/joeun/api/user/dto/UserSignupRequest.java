package com.joeun.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequest {

  @NotNull(message = "universityId is required")
  @Positive(message = "universityId must be a positive number")
  private Long universityId;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  @Size(max = 100)
  private String name;

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "올바른 이메일 형식을 입력해주세요.")
  @Size(max = 120)
  private String email;

  @NotBlank(message = "학번은 필수 입력값입니다.")
  @Size(max = 64)
  private String studentId;

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 64, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  private String password;
}
