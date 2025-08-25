package com.joeun.api.user.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMeResponse {
  private Long id;
  private Long universityId;
  private String name;
  private String studentId;
  private String email;
  private List<String> roles;
}