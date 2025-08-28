package com.joeun.api.user.controller;

import com.joeun.api.user.dto.UserBankAccountCreateRequest;
import com.joeun.api.user.dto.UserBankAccountCreateResponse;
import com.joeun.api.user.dto.UserBankAccountResponse;
import com.joeun.api.user.dto.UserMeResponse;
import com.joeun.api.user.dto.UserSigninRequest;
import com.joeun.api.user.dto.UserSigninResponse;
import com.joeun.api.user.dto.UserSignupRequest;
import com.joeun.api.user.service.UserService;
import com.joeun.global.config.LoginUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/signup")
  public ResponseEntity<?> signup(
          @Valid @RequestBody UserSignupRequest dto
  ){
    userService.createUser(dto);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/signin")
  public ResponseEntity<UserSigninResponse> signin(
          @Valid @RequestBody UserSigninRequest request,
          HttpServletResponse response
  ){
    UserSigninResponse body = userService.login(request);

    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", body.getRefreshToken())
            .httpOnly(true)
            .secure(false)      // 로컬: false / 운영(HTTPS): true
            .sameSite("Lax")    // 운영에서 크로스 도메인이면 "None" + secure(true)
            .path("/")
            .maxAge(Duration.ofDays(14))
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    return ResponseEntity.ok(body);
  }

  @GetMapping("/me")
  public ResponseEntity<UserMeResponse> me(
          @AuthenticationPrincipal LoginUser user
  ) {
    // 인증 실패 시 Security가 401/403 처리하므로 여기 오면 이미 user는 null 아님
    var body = UserMeResponse.builder()
            .id(user.id())
            .universityId(user.universityId())
            .name(user.name())
            .studentId(user.studentId())
            .email(user.email())
            .roles(user.authorities().stream().map(a -> a.getAuthority()).toList())
            .build();

    return ResponseEntity.ok(body);
  }

  @GetMapping("/me/bank-accounts")
  public ResponseEntity<List<UserBankAccountResponse>> myBankAccounts(
          @AuthenticationPrincipal LoginUser user
  ) {
    return ResponseEntity.ok(userService.listMyBankAccounts(user.id()));
  }

  @PostMapping("/me/bank-accounts")
  public ResponseEntity<UserBankAccountCreateResponse> addMyBankAccount(
          @AuthenticationPrincipal LoginUser loginUser,
          @Valid @RequestBody UserBankAccountCreateRequest req
  ) {
    var body = userService.addMyBankAccount(loginUser.id(), req);
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }
}