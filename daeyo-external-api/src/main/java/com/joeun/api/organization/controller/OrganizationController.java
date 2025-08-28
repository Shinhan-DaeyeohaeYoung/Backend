package com.joeun.api.organization.controller;

import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.dto.OrgBankAccountResponse;
import com.joeun.api.organization.dto.OrganizationJoinRequest;
import com.joeun.api.organization.dto.UserOrgMembershipResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.global.config.LoginUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {

  private final OrganizationService orgService;

/*  @PostMapping
  public ResponseEntity<UserOrgMembershipResponse> joinOrganization(
       @RequestBody @Valid OrganizationJoinRequest req,
      @AuthenticationPrincipal LoginUser loginUser
   ) {
         var membership = orgService.joinOrganization(loginUser, req.getOrganizationId());
         return ResponseEntity.status(HttpStatus.CREATED).body(UserOrgMembershipResponse.from(membership));
  }*/

  @GetMapping
  public ResponseEntity<List<MyOrganizationResponse>> getMyOrganizations(
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(name = "type", required = false) String type
  ) {
    return ResponseEntity.ok(orgService.getMyOrganizations(loginUser, type));
  }

  /*
  내 조직 상세보기
   */
  @GetMapping("/{id}")
  public ResponseEntity<MyOrganizationResponse> getMyOrganization(
      @AuthenticationPrincipal LoginUser loginUser,
      @PathVariable("id") Long organizationId
  ) {
    return ResponseEntity.ok(orgService.getMyOrganization(loginUser, organizationId));
  }

  @GetMapping("/{id}/bank-accounts")
  public ResponseEntity<List<OrgBankAccountResponse>> getOrganizationAccounts(
      @PathVariable("id") Long orgId
      // , @AuthenticationPrincipal LoginUser loginUser
  ) {
    List<OrgBankAccountResponse> body = orgService.listOrganizationBankAccounts(orgId /*, loginUser*/);
    return ResponseEntity.ok(body);
  }

}
