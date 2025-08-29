package com.joeun.api.organization.service;

import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.dto.OrgBankAccountResponse;
import com.joeun.api.organization.mapper.OrgBankAccountMappers;
import com.joeun.api.ssafyAPI.client.SsafyDemandDepositClient;
import com.joeun.api.ssafyAPI.dto.AccountApiHeader;
import com.joeun.api.ssafyAPI.dto.InquireDemandDepositAccountBalanceRequest;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.global.config.LoginUser;
import com.joeun.service.organization.OrganizationDomainService;
import com.joeun.service.user.UserDomainService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationDomainService organizationDomainService;
  private final UserDomainService userDomainService;
  private final SsafyDemandDepositClient ssafyDemandDepositClient;

  @Value("${ssafy.api-key}")
  private String ssafyAdminApiKey;

/*  public UserOrgMembership joinOrganization(LoginUser loginUser, Long organizationId) {
    User user = userDomainService.findById(loginUser.id())
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
           HttpStatus.NOT_FOUND,
            "User not found"
        ));

    Organization org = organizationDomainService.getOrganization(organizationId);

    if (user.getUniversity() != null && org.getUniversity() != null) {
      if (!user.getUniversity().getId().equals(org.getUniversity().getId())) {
        throw new org.springframework.web.server.ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "사용자 소속 대학과 조직의 대학이 다릅니다."
        );
      }
    }
    // 자식 → 부모로 올라가며 전부 membership 추가 (중복은 skip)
    organizationDomainService.addMemberCascade(user, org);

    // 가입 결과(요청한 자식 조직의 membership) 반환
    return organizationDomainService.getMembership(user.getId(), org.getId())
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Membership not created"
        ));
  }*/

  public List<MyOrganizationResponse> getMyOrganizations(LoginUser loginUser, String type) {
    User user = userDomainService.findById(loginUser.id())
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

    // Domain Service에서 Domain 엔티티 리스트 반환
    List<UserOrgMembership> memberships;
    if (type == null || type.isBlank()) {
      memberships = organizationDomainService.findMembershipsByUser(user.getId());
    } else {
      OrganizationType ot = OrganizationType.valueOf(type);
      memberships = organizationDomainService.findMembershipsByUserAndType(user.getId(), ot);
    }

    // Entity → DTO 변환은 Application Service 책임
    return memberships.stream()
        .map(m -> MyOrganizationResponse.builder()
            .organizationId(m.getOrganization().getId())
            .universityId(m.getOrganization().getUniversity() != null
                ? m.getOrganization().getUniversity().getId() : null)
            .name(m.getOrganization().getName())
            .type(m.getOrganization().getType().name())
            .parentOrganizationId(m.getOrganization().getParent() != null
                ? m.getOrganization().getParent().getId() : null)
            .isActive(Boolean.TRUE.equals(m.getOrganization().isActive()))
            .role(m.getRole().name())
            .build())
        .toList();
  }

  public MyOrganizationResponse getMyOrganization(LoginUser loginUser, Long organizationId) {
    User user = userDomainService.findById(loginUser.id())
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "User not found"));

    UserOrgMembership membership = organizationDomainService.findMembership(user.getId(), organizationId)
        .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
            org.springframework.http.HttpStatus.NOT_FOUND, "Membership not found"));

    return MyOrganizationResponse.builder()
        .organizationId(membership.getOrganization().getId())
        .universityId(membership.getOrganization().getUniversity() != null
            ? membership.getOrganization().getUniversity().getId() : null)
        .name(membership.getOrganization().getName())
        .type(membership.getOrganization().getType().name())
        .parentOrganizationId(membership.getOrganization().getParent() != null
            ? membership.getOrganization().getParent().getId() : null)
        .isActive(Boolean.TRUE.equals(membership.getOrganization().isActive()))
        .role(membership.getRole().name())
        .build();
  }

  public List<OrgBankAccountResponse> listOrganizationBankAccounts(Long orgId /*, LoginUser loginUser*/) {
    var accounts = organizationDomainService.findOrgBankAccounts(orgId);
    return accounts.stream()
        .map(OrgBankAccountMappers::toResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public OrgBankAccountResponse getOrganizationPrimaryAccountWithBalance(Long orgId) {
    // 1) 조직 존재 검증 + 주계좌(없으면 첫 계좌) 조회
    var primary = organizationDomainService.findPrimaryOrgBankAccountOrFirstOrThrow(orgId);

    // 2) org userKey 조회
    String userKey = organizationDomainService.getOrgUserKeyOrThrow(orgId);

    // 3) 외부 잔액 조회 호출
    var header = AccountApiHeader.builder()
        .apiName("inquireDemandDepositAccountBalance")
        .transmissionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        .transmissionTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")))
        .institutionCode("00100")
        .fintechAppNo("001")
        .apiServiceCode("inquireDemandDepositAccountBalance")
        .institutionTransactionUniqueNo(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 999999))
        .apiKey(ssafyAdminApiKey)
        .userKey(userKey)
        .build();

    var req = InquireDemandDepositAccountBalanceRequest.builder()
        .header(header)
        .accountNo(primary.getAccountNo()) // 조직은 평문 저장
        .build();

    var res = ssafyDemandDepositClient.inquireDemandDepositAccountBalance(req);
    if (res == null || res.getHeader() == null || !"H0000".equals(res.getHeader().getResponseCode())) {
      String msg = (res != null && res.getHeader() != null) ? res.getHeader().getResponseMessage() : "upstream error";
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "org balance inquiry failed: " + msg);
    }

    String balanceStr = (res.getRec() != null) ? res.getRec().getAccountBalance() : null;
    if (balanceStr == null) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "missing accountBalance in response");
    }

    BigDecimal balance;
    try { balance = new BigDecimal(balanceStr); }
    catch (NumberFormatException nfe) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "invalid accountBalance format");
    }

    // 4) DTO로 변환 (잔액 포함)
    return OrgBankAccountMappers.toResponse(primary, balance);
  }

}