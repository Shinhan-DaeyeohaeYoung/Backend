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
import com.joeun.api.deposit.dto.OrgDepositResponse;
import com.joeun.api.deposit.mapper.DepositMappers;
import com.joeun.api.ssafyAPI.client.SsafyDemandDepositClient;
import com.joeun.api.ssafyAPI.dto.AccountApiHeader;
import com.joeun.api.ssafyAPI.dto.UpdateDemandDepositAccountTransferRequest;
import com.joeun.api.ssafyAPI.dto.UpdateDemandDepositAccountTransferResponse;
import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.types.DepositEventType;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.domain.users.types.UserOrgRole;
import com.joeun.global.config.LoginUser;
import com.joeun.global.util.AccountCipher;
import com.joeun.service.deposit.DepositDomainService;
import com.joeun.service.deposit.DepositEventView;
import com.joeun.service.deposit.OrgDepositEventView;
import com.joeun.service.organization.OrganizationDomainService;
import com.joeun.service.user.UserDomainService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

  private final UserDomainService userDomainService;
  private final DepositDomainService depositDomainService;
  private final OrganizationDomainService orgDomainService;
  private final AccountCipher accountCipher;

  private final SsafyDemandDepositClient ssafyClient;
  @Value("${ssafy.api-key}")
  private String ssafyAdminApiKey;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final ZoneId Z = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HHmmss");
  private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final Logger log = LoggerFactory.getLogger(DepositService.class);

  public List<DepositResponse> listMyDepositHistory(Long userId, String statusText) {
    final DepositEventType filter = parseStatus(statusText);
    List<DepositEventView> views = depositDomainService.findUserDepositEventViews(userId, filter);

    return views.stream()
        .map(v -> DepositResponse.from(
            v.eventId(), v.amount(), v.eventType(), v.occurredAt(), v.organizationName()
        ))
        .toList();
  }

  // ---- helpers ----
  private DepositEventType parseStatus(String s) {
    if (s == null || s.isBlank()) return null;
    String u = s.trim().toUpperCase();
    return switch (u) {
      case "예치", "CREATED"   -> DepositEventType.CREATED;
      case "환불", "REFUNDED"  -> DepositEventType.REFUNDED;
      case "몰수", "FORFEITED" -> DepositEventType.FORFEITED;
      default -> null; // 알 수 없는 입력은 필터 미적용(전체)
    };
  }

/*
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
  }*/

  public List<OrgDepositResponse> listOrganizationDepositHistory(LoginUser actor,
      Long orgId,
      String statusText) {
    // 1) 권한 확인: 조직 관리자만 허용(정책에 맞게 조정)
    boolean isAdmin = orgDomainService.existsByUserOrgAndRole(actor.id(), orgId, UserOrgRole.ORG_ADMIN);
    if (!isAdmin) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "org admin required");
    }

    // 2) 상태 필터 파싱 (사용자 관점과 동일 로직 재사용)
    DepositEventType filter = parseStatus(statusText);

    // 3) 도메인 서비스 조회 (스칼라 read model)
    List<OrgDepositEventView> views = depositDomainService.findOrganizationDepositEventViews(orgId, filter);

    // 4) API DTO 매핑
    return views.stream()
        .map(v -> OrgDepositResponse.from(
            v.eventId(), v.amount(), v.eventType(), v.occurredAt(), v.userName()
        ))
        .toList();
  }
/*
  public DepositCreateResponse createDeposit(
      Long userId,
      DepositCreateRequest req
  ) {
    try {
      Deposit saved = depositDomainService.createDeposit(
          req.getUser_id(),
          req.getOrganization_id(),
          req.getAmount(),
          *//* rentalId *//* null,
          req.getUniversity_id()
      );
      return DepositMappers.toCreateResponse(saved);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), e);
    }
  }*/

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

  public void transferUserToOrganization(Long id, Long o, BigDecimal amount, String memo) {
    // 0) 금액 검증
    if (amount == null || amount.signum() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
    }

    // 1) 사용자 userKey
    final String userKey;
    try {
      userKey = userDomainService.getUserKeyOrThrow(id);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "userKey not found for userId=" + id);
    }

    // 2) 사용자 주계좌 복호화
    var userPrimary = userDomainService.findPrimaryBankAccount(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user primary bank account not found"));
    byte[] enc = userPrimary.getAccountNo(); // byte[] (암호문)
    if (enc == null || enc.length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user encrypted account not found");
    }
    String withdrawalAccountNo = accountCipher.decrypt(enc); // 평문

    // 3) 조직 주계좌(평문)
    var orgAcc = orgDomainService.findPrimaryOrgBankAccountOrFirstOrThrow(o);
    String depositAccountNo = orgAcc.getAccountNo();

    // 4) SSAFY 헤더/요청 구성
    String today = LocalDate.now(Z).format(D);
    String now   = LocalTime.now(Z).format(T);
    String idem  = LocalDateTime.now().format(TS) + ThreadLocalRandom.current().nextInt(100000, 999999);
    String amountStr = amount.setScale(0, RoundingMode.DOWN).toPlainString(); // 원단위 정수 문자열

    AccountApiHeader header = AccountApiHeader.builder()
        .apiName("updateDemandDepositAccountTransfer")
        .transmissionDate(today)
        .transmissionTime(now)
        .institutionCode("00100")
        .fintechAppNo("001")
        .apiServiceCode("updateDemandDepositAccountTransfer")
        .institutionTransactionUniqueNo(idem)
        .apiKey(ssafyAdminApiKey)
        .userKey(userKey) // ✅ 출금 주체는 사용자
        .build();

    UpdateDemandDepositAccountTransferRequest req =
        UpdateDemandDepositAccountTransferRequest.builder()
            .header(header)
            .depositAccountNo(depositAccountNo)
            .depositTransactionSummary(
                (memo != null && !memo.isBlank()) ? memo : "(수시입출금) : 입금(이체)")
            .transactionBalance(amountStr)
            .withdrawalAccountNo(withdrawalAccountNo)
            .withdrawalTransactionSummary("(수시입출금) : 출금(이체)")
            .build();

    // 5) 호출
    UpdateDemandDepositAccountTransferResponse res = ssafyClient.updateDemandDepositAccountTransfer(req);

    // 6) 응답 검증
    if (res == null || res.getHeader() == null || !"H0000".equals(res.getHeader().getResponseCode())) {
      String msg = (res != null && res.getHeader() != null)
          ? res.getHeader().getResponseMessage()
          : "upstream error";
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "transfer failed: " + msg);
    }

    log.info("Deposit transfer success: idem={}, userId={}, orgId={}, amount={}",
        header.getInstitutionTransactionUniqueNo(), id, o, amountStr);
  }

  /** 조직(orgId) → 사용자(userId) 환불 이체 */
  public void transferOrganizationToUser(Long orgId, Long userId, BigDecimal amount, String memo) {
    if (amount == null || amount.signum() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amount must be positive");
    }

    // 1) 조직 userKey + 출금계좌(평문)
    String orgUserKey = orgDomainService.getOrgUserKeyOrThrow(orgId);
    var orgAcc = orgDomainService.findPrimaryOrgBankAccountOrFirstOrThrow(orgId);
    String withdrawalAccountNo = orgAcc.getAccountNo(); // 조직 출금

    // 2) 사용자 입금계좌(복호화)
    var userPrimary = userDomainService.findPrimaryBankAccount(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "user primary bank account not found"));
    byte[] enc = userPrimary.getAccountNo();
    if (enc == null || enc.length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user encrypted account not found");
    }
    String depositAccountNo = accountCipher.decrypt(enc); // 사용자 입금

    ZonedDateTime nowKst = ZonedDateTime.now(KST);
    String today = nowKst.format(D);
    String time  = nowKst.format(T);
    String idem  = nowKst.format(TS) + ThreadLocalRandom.current().nextInt(100000, 999999);
    String amountStr = amount.setScale(0, RoundingMode.DOWN).toPlainString();

    AccountApiHeader header = AccountApiHeader.builder()
        .apiName("updateDemandDepositAccountTransfer")
        .transmissionDate(today)     // KST 날짜
        .transmissionTime(time)      // KST 시간
        .institutionCode("00100")
        .fintechAppNo("001")
        .apiServiceCode("updateDemandDepositAccountTransfer")
        .institutionTransactionUniqueNo(idem)
        .apiKey(ssafyAdminApiKey)
        .userKey(orgUserKey)         // 출금 주체 = 조직
        .build();

    UpdateDemandDepositAccountTransferRequest req = UpdateDemandDepositAccountTransferRequest.builder()
        .header(header)
        .depositAccountNo(depositAccountNo) // 사용자 입금
        .depositTransactionSummary((memo != null && !memo.isBlank()) ? memo : "(수시입출금) : 입금(이체)")
        .transactionBalance(amountStr)
        .withdrawalAccountNo(withdrawalAccountNo) // 조직 출금
        .withdrawalTransactionSummary("(수시입출금) : 출금(이체)")
        .build();

    var res = ssafyClient.updateDemandDepositAccountTransfer(req);
    if (res == null || res.getHeader() == null || !"H0000".equals(res.getHeader().getResponseCode())) {
      String msg = (res != null && res.getHeader() != null) ? res.getHeader().getResponseMessage() : "upstream error";
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "refund transfer failed: " + msg);
    }
  }

/*

  public DepositRefundResponse refundFull(Long depositId, Long userId) {
    final Deposit d;

    try {
      d = depositDomainService.getByIdWithAllJoinsOrThrow(depositId);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 소유자 검증
    if (d.getUser() == null || !userId.equals(d.getUser().getId())) {
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
      var updated = depositDomainService.refundFull(d, userId);
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
*/

/*  public DepositForfeitResponse forfeitFull(Long depositId, Long userId) {
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
        userId, d.getOrganization().getId(), UserOrgRole.ORG_ADMIN);
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
      var updated = depositDomainService.forfeitFull(d, userId);
      return DepositForfeitResponse.builder()
          .id(updated.getId())
          .status(updated.getStatus().name())
          .updatedAt(updated.getUpdatedAt())
          .build();
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }*/

}
