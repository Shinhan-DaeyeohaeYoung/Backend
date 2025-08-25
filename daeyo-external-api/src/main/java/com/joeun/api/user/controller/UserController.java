package com.joeun.api.user.controller;

import com.joeun.api.user.dto.UserSigninRequest;
import com.joeun.api.user.dto.UserSignupRequest;
import com.joeun.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("signup")
  public ResponseEntity<?> signup(
      @Valid @RequestBody UserSignupRequest dto
  ){
    userService.createUser(dto);
    return ResponseEntity.ok().build();
  }

//  @PostMapping("signin")
//  public ResponseEntity<?> signin(
//      @Valid @RequestBody UserSigninRequest dto
//  ){
//    userService.login(dto);
//
//  }

}
