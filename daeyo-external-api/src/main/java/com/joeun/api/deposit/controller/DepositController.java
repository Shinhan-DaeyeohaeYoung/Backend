package com.joeun.api.deposit.controller;

import com.joeun.api.deposit.dto.DepositResponse;
import com.joeun.api.deposit.service.DepositService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deposits")
@RequiredArgsConstructor
public class DepositController {

  private DepositService depositService;

  @GetMapping
  public ResponseEntity<List<DepositResponse>> getMyDeposits(
      @AuthenticationPrincipal(expression = "id") Long userId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    var result = depositService.searchMyDepositsSimple(userId, status, page, size);
    return ResponseEntity.ok()
        .header("X-Total-Count", String.valueOf(result.totalElements()))
        .body(result.content());
  }

}
