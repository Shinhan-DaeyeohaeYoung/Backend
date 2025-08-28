package com.joeun.api.students.controller;

import com.joeun.api.students.dto.StudentCreateRequest;
import com.joeun.api.students.dto.StudentResponse;
import com.joeun.api.students.dto.StudentSignupRequest;
import com.joeun.api.students.dto.StudentSignupResponse;
import com.joeun.api.students.dto.StudentVerifyRequest;
import com.joeun.api.students.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {
  private final StudentService studentService;

  // POST /api/students/verify
  @PostMapping("/verify")
  public ResponseEntity<StudentResponse> verifyStudent(@RequestBody @Valid StudentVerifyRequest req) {
    StudentResponse resp = studentService.verifyAndFetch(req);
    return ResponseEntity.ok(resp);
  }

  @PostMapping
  public ResponseEntity<StudentResponse> create(@RequestBody @Valid StudentCreateRequest req) {
    StudentResponse resp = studentService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  @PostMapping("/signup")
  public ResponseEntity<StudentSignupResponse> signup(@RequestBody @Valid StudentSignupRequest req) {
    StudentSignupResponse resp = studentService.signup(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

}
