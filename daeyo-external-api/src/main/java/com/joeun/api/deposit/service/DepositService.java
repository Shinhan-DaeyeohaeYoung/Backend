package com.joeun.api.deposit.service;

import com.joeun.api.deposit.dto.DepositListResult;
import com.joeun.api.deposit.dto.DepositResponse;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.service.deposit.DepositDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DepositService {

  private final DepositDomainService depositDomainService;

  public DepositListResult searchMyDepositsSimple(Long userId, String status, int page, int size) {
    DepositStatus statusEnum = parseStatus(status);
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    var resultPage = depositDomainService.searchUserDepositsSimple(userId, statusEnum, pageable);

    List<DepositResponse> content = resultPage.getContent().stream()
        .map(DepositResponse::from)
        .toList();

    return new DepositListResult(content, resultPage.getTotalElements());
  }

  private DepositStatus parseStatus(String status) {
    if (status == null || status.isBlank()) return null;
    try {
      return DepositStatus.valueOf(status.trim().toUpperCase()); // HELD/RELEASED/FORFEITED
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + status);
    }
  }
}
