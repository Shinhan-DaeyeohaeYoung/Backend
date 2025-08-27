package com.joeun.api.deposit.service;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.joeun.api.deposit.dto.DepositAdminDetailResponse;
import com.joeun.api.deposit.dto.DepositCreateRequest;
import com.joeun.api.deposit.dto.DepositCreateResponse;
import com.joeun.api.deposit.dto.DepositForfeitResponse;
import com.joeun.api.deposit.dto.DepositListDto;
import com.joeun.api.deposit.dto.DepositListResult;
import com.joeun.api.deposit.dto.DepositRefundResponse;
import com.joeun.api.deposit.dto.DepositResponse;
import com.joeun.api.deposit.mapper.DepositMappers;
import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.domain.users.types.UserOrgRole;
import com.joeun.global.config.LoginUser;
import com.joeun.service.deposit.DepositDomainService;
import com.joeun.service.organization.OrganizationDomainService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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

  public DepositAdminDetailResponse getDepositDetail(Long depositId, LoginUser actor) {
    // 1) 대상 조회 (없으면 내부에서 404 던짐)
    Deposit d = depositDomainService.getByIdOrThrow(depositId);

    // 2) (선택) 테넌시 일치 검증
    if (actor.universityId() != null && d.getUniversity() != null) {
      if (!actor.universityId().equals(d.getUniversity().getId())) {
        throw new ResponseStatusException(FORBIDDEN, "not allowed (tenant mismatch)");
      }
    }

    // 4) ORG_ADMIN 멤버십 검증 (사용자-조직 중간테이블)
    boolean allowed = orgDomainService.existsByUserOrgAndRole(
        actor.id(),
        d.getOrganization().getId(),
        UserOrgRole.ORG_ADMIN
    );
    if (!allowed) {
      throw new ResponseStatusException(FORBIDDEN, "not allowed (org admin required)");
    }

    // 5) 엔티티 → 응답 DTO 매핑
    return DepositAdminDetailResponse.builder()
        .id(d.getId())
        .universityId(d.getUniversity() == null ? null : d.getUniversity().getId())
        .organizationId(d.getOrganization() == null ? null : d.getOrganization().getId())
        .userId(d.getUser() == null ? null : d.getUser().getId())
        .amount(d.getAmount())
        .status(d.getStatus().name())
        .createdAt(d.getCreatedAt())
        .updatedAt(d.getUpdatedAt())
        .refundAccountId(d.getRefundAccount() == null ? null : d.getRefundAccount().getId())
        .build();
  }

  public DepositRefundResponse refundFull(Long depositId, LoginUser actor) {
    final Deposit d;
    try {
      d = depositDomainService.getByIdWithAllJoinsOrThrow(depositId);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 소유자 검증
    if (d.getUser() == null || !actor.id().equals(d.getUser().getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only owner can refund");
    }

    // 상태 검증
    if (d.getStatus() != DepositStatus.HELD) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "invalid state: " + d.getStatus());
    }

    // 환불 계좌 존재 검증 (사전 지정)
    if (d.getRefundAccount() == null) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "refund account not set");
    }

    try {
      // 전액 환불 상태 전이 수행
      var updated = depositDomainService.refundFull(d, actor.id());
      return DepositRefundResponse.builder()
          .id(updated.getId())
          .status(updated.getStatus().name())
          .updatedAt(updated.getUpdatedAt())
          .build();
    } catch (IllegalStateException e) {
      // 도메인 전이 규칙 위반 등
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }
  }

  public DepositForfeitResponse forfeitFull(Long depositId, LoginUser actor) {
    final Deposit d;
    try {
      d = depositDomainService.getByIdWithAllJoinsOrThrow(depositId);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 조직 존재 여부
    if (d.getOrganization() == null) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "no organization bound to deposit");
    }

    boolean isAdmin = orgDomainService.existsByUserOrgAndRole(
        actor.id(), d.getOrganization().getId(), UserOrgRole.ORG_ADMIN);
    if (!isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "org admin required");
    }

    // 상태 전이 규칙
    if (d.getStatus() == DepositStatus.FORFEITED) {
      // 멱등처리: 이미 몰수 상태면 그대로 200 반환
      return DepositForfeitResponse.builder()
          .id(d.getId())
          .status(d.getStatus().name())
          .updatedAt(d.getUpdatedAt())
          .build();
    }
    if (d.getStatus() != DepositStatus.HELD) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "invalid state: " + d.getStatus());
    }

    try {
      var updated = depositDomainService.forfeitFull(d, actor.id());
      return DepositForfeitResponse.builder()
          .id(updated.getId())
          .status(updated.getStatus().name())
          .updatedAt(updated.getUpdatedAt())
          .build();
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

}
