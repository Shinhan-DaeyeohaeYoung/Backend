package com.joeun.api.organization.controller;

import com.joeun.api.organization.dto.OrganizationCreateRequest;
import com.joeun.api.organization.dto.OrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.global.config.LoginUser;
import jakarta.validation.Valid;
import java.nio.file.attribute.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService orgService;

  @PostMapping
  public ResponseEntity<OrganizationResponse> createOrganization(
      @RequestBody @Valid OrganizationCreateRequest request,
      @AuthenticationPrincipal LoginUser user
  ) {
    OrganizationResponse response = orgService.createOrganization(user, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

}
