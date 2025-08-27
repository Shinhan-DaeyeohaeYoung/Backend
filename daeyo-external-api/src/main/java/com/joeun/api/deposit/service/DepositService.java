package com.joeun.api.deposit.service;

import com.joeun.api.deposit.dto.DepositCreateRequest;
import com.joeun.api.deposit.dto.DepositCreateResponse;
import com.joeun.api.deposit.dto.DepositListDto;
import com.joeun.api.deposit.dto.DepositListResult;
import com.joeun.api.deposit.dto.DepositResponse;
import com.joeun.api.deposit.mapper.DepositMappers;
import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.global.config.LoginUser;
import com.joeun.service.deposit.DepositDomainService;
import com.joeun.service.organization.OrganizationDomainService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DepositService {

  private final DepositDomainService depositDomainService;
  private final OrganizationDomainService orgDomainService;

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

  public List<DepositListDto> listOrganizationDeposits(
      LoginUser loginUser, Long orgId, Set<DepositStatus> statuses) {

    if (!orgDomainService.isOrgAdmin(loginUser.id(), orgId)) {
      throw new AccessDeniedException("ORG_ADMIN required for organization " + orgId);
    }

    final Set<DepositStatus> safeStatuses = (statuses == null) ? Collections.emptySet() : statuses;
    final boolean hasStatuses = !safeStatuses.isEmpty();

    List<Deposit> deposits = depositDomainService.findByOrganization(orgId, hasStatuses, safeStatuses);

    List<DepositListDto> dtos = new ArrayList<>(deposits.size());
    for (Deposit d : deposits) {
      dtos.add(DepositMappers.toListDto(d));
    }
    return dtos;
  }

  public DepositCreateResponse createDeposit(LoginUser loginUser, DepositCreateRequest req) {
    try {
      Deposit saved = depositDomainService.createDeposit(
          req.getUser_id(),
          req.getOrganization_id(),
          req.getAmount(),
          /* rentalId */ null,
          req.getUniversity_id()
      );
      return DepositMappers.toCreateResponse(saved);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    }
  }


}
