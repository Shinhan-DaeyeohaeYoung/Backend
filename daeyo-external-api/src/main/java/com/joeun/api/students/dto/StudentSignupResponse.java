package com.joeun.api.students.dto;

import java.util.List;

public record StudentSignupResponse(
    UserDto user,
    List<UserMembershipDto> memberships
) {
  public static record UserDto(Long id, Long universityId, String name, String email, String studentId, String role) {}
}