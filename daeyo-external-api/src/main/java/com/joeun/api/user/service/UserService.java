package com.joeun.api.user.service;

import com.joeun.api.user.dto.UserSignupRequest;
import com.joeun.domain.users.entity.User;

import com.joeun.domain.university.entity.University;
import com.joeun.service.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserDomainService userDomainService;
  private final PasswordEncoder encoder;

  public void createUser(UserSignupRequest request) {
    University univ = userDomainService.getUniversity(request.getUniversityId());

    User user = User.builder()
        .university(univ)
        .name(request.getName())
        .email(request.getEmail())
        .studentId(request.getStudentId())
        .passwordHash(encoder.encode(request.getPassword()))
        .role("USER")
        .build();

    userDomainService.createUser(user);
  }

}
