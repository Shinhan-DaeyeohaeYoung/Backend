package com.joeun.api.user.service;

import com.joeun.api.exception.UnauthorizedException;
import com.joeun.api.user.dto.UserSigninRequest;
import com.joeun.api.user.dto.UserSigninResponse;
import com.joeun.api.user.dto.UserSignupRequest;
import com.joeun.domain.users.entity.User;

import com.joeun.domain.university.entity.University;
import com.joeun.global.util.JwtUtil;
import com.joeun.service.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserDomainService userDomainService;
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;

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

  public UserSigninResponse login(UserSigninRequest req){
    // 1) 사용자 조회 (학교+학번)
    User user = userDomainService.findByUnivAndStudentId(req.getUniversityId(), req.getStudentId())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    // 2) 비밀번호 검증
    if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    // 3) 토큰 발급
    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    // 4) 응답
    return new UserSigninResponse(accessToken, refreshToken, user.getName());
  }

}
