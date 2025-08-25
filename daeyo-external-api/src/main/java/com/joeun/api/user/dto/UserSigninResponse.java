package com.joeun.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSigninResponse {
  private final String accessToken;
  private final String refreshToken;
  private final String name;
}