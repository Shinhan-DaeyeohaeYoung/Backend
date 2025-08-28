package com.joeun.api.deposit.controller;

import com.joeun.api.deposit.dto.DepositAdminDetailResponse;
import com.joeun.api.deposit.dto.DepositCreateRequest;
import com.joeun.api.deposit.dto.DepositCreateResponse;
import com.joeun.api.deposit.dto.DepositForfeitResponse;
import com.joeun.api.deposit.dto.DepositListDto;
import com.joeun.api.deposit.dto.DepositRefundResponse;
import com.joeun.api.deposit.dto.DepositResponse;
import com.joeun.api.deposit.dto.OrgDepositResponse;
import com.joeun.api.deposit.service.DepositService;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.global.config.LoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
@Slf4j
public class DepositController {

  private final DepositService depositService;

  @GetMapping
  public ResponseEntity<List<DepositResponse>> getMyDeposits(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(required = false) String status // "예치/환불/몰수" or "CREATED/REFUNDED/FORFEITED"
  ) {
    List<DepositResponse> history = depositService.listMyDepositHistory(userId, status);
    return ResponseEntity.ok(history); // 페이징 제거
  }

/*
  @GetMapping("/organizations/{orgId}")
  public ResponseEntity<List<DepositListDto>> listOrgDeposits(
      @PathVariable("orgId") @NotNull Long orgId,
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(name = "status", required = false) String statusCsv // 예: HELD,RELEASED
  ) {
    // log.info("loginUser={}", loginUser.id());
    Set<DepositStatus> statuses = parseStatuses(statusCsv);
    List<DepositListDto> result = depositService.listOrganizationDeposits(loginUser, orgId, statuses);
    return ResponseEntity.ok(result);
  }
*/

  @GetMapping("/organizations/{orgId}")
  public ResponseEntity<List<OrgDepositResponse>> listOrgDeposits(
      @PathVariable("orgId") @NotNull Long orgId,
      @AuthenticationPrincipal LoginUser loginUser,
      @RequestParam(name = "status", required = false) String status // 예: 예치/환불/몰수 또는 CREATED/REFUNDED/FORFEITED
  ) {
    var rows = depositService.listOrganizationDepositHistory(loginUser, orgId, status);
    return ResponseEntity.ok(rows);
  }

  private Set<DepositStatus> parseStatuses(String csv) {
    if (csv == null || csv.isBlank()) return Collections.emptySet();
    return Arrays.stream(csv.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .map(s -> DepositStatus.valueOf(s.toUpperCase(Locale.ROOT)))
        .collect(Collectors.toCollection(() -> EnumSet.noneOf(DepositStatus.class)));
  }

  @PostMapping
  public ResponseEntity<DepositCreateResponse> create(
      @AuthenticationPrincipal LoginUser loginUser,
      @Valid @RequestBody DepositCreateRequest req
  ) {
    if (loginUser == null) return ResponseEntity.status(401).build();

    // 멤버/관리자 무관, 본인 보증금만 생성 가능
    if (!loginUser.id().equals(req.getUser_id())) {
      return ResponseEntity.status(403).build();
    }

    DepositCreateResponse created = depositService.createDeposit(loginUser, req);
    return ResponseEntity
        .created(URI.create("/api/deposits/" + created.getId()))
        .body(created);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DepositAdminDetailResponse> getDeposit(
      @PathVariable("id") Long depositId,
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    var dto = depositService.getDepositDetail(depositId, loginUser);
    return ResponseEntity.ok(dto);
  }

  @PostMapping("/{id}/refund")
  public ResponseEntity<DepositRefundResponse> refundFull(
      @PathVariable("id") Long depositId,
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    var resp = depositService.refundFull(depositId, loginUser);
    return ResponseEntity.ok(resp);
  }


  @PostMapping("/{id}/forfeit")
  public ResponseEntity<DepositForfeitResponse> forfeitFull(
      @PathVariable("id") Long depositId,
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    var resp = depositService.forfeitFull(depositId, loginUser);
    return ResponseEntity.ok(resp);
  }
}
