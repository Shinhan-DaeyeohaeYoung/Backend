package com.joeun.api.students.dto;

public record StudentResponse(
    Long id,
    Long universityId,
    String studentNo,
    String name,
    String email,
    String signupStatus
) {}