package com.joeun.service.organization;

import com.joeun.domain.organization.entity.OrgApiCredential;
import com.joeun.domain.organization.entity.OrgBankAccount;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.repository.OrgApiCredentialRepository;
import com.joeun.domain.organization.repository.OrgBankAccountRepository;
import com.joeun.domain.organization.repository.OrganizationRepository;
import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.repository.UserOrgMembershipRepository;
import com.joeun.domain.users.types.UserOrgRole;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationDomainService {

  private final OrganizationRepository organizationRepository;
  private final OrgBankAccountRepository orgBankAccountRepository;
  private final UserOrgMembershipRepository membershipRepository;
  private final OrgApiCredentialRepository orgApiCredentialRepository;

  public Organization getOrganization(Long id) {
    return organizationRepository.findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Organization not found"));
  }

  @Transactional(readOnly = true)
  public OrgBankAccount findPrimaryOrgBankAccountOrFirstOrThrow(Long orgId) {
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new IllegalStateException("organization not found: " + orgId));

    return orgBankAccountRepository.findFirstByOrganizationIdAndIsPrimaryTrue(orgId)
        .orElseGet(() -> {
          var list = orgBankAccountRepository.findAllByOrganizationId(orgId);
          if (list.isEmpty()) throw new IllegalStateException("organization bank account not found: " + orgId);
          return list.get(0); // 조직은 1계좌 가정
        });
  }

  public Optional<UserOrgMembership> getMembership(Long userId, Long organizationId) {
    return membershipRepository.findByUserIdAndOrganizationId(userId, organizationId);
  }

  @Transactional
  public List<UserOrgMembership> findMembershipsByUser(Long userId) {
    return membershipRepository.findAllWithOrganizationByUserId(userId);
  }

  @Transactional
  public List<UserOrgMembership> findMembershipsByUserAndType(Long userId, OrganizationType type) {
    return membershipRepository.findAllWithOrganizationByUserIdAndType(userId, type);
  }

  @Transactional
  public Optional<UserOrgMembership> findMembership(Long userId, Long organizationId) {
    return membershipRepository.findByUserIdAndOrganizationId(userId, organizationId);
  }

  public boolean isOrgAdmin(Long userId, Long orgId) {
    return membershipRepository
        .existsByUserIdAndOrganizationIdAndRole(userId, orgId, UserOrgRole.ORG_ADMIN);
  }

  @Transactional
  public boolean existsByUserOrgAndRole(Long userId, Long organizationId, UserOrgRole role) {
    return membershipRepository.existsByUserIdAndOrganizationIdAndRole(userId, organizationId, role);
  }

  @Transactional
  public List<OrgBankAccount> findOrgBankAccounts(Long orgId) {
    // 조직 존재 검증(옵션이지만 안전)
    organizationRepository.findById(orgId)
        .orElseThrow(() -> new IllegalStateException("organization not found: " + orgId));
    return orgBankAccountRepository.findAllByOrganizationId(orgId);
  }

  @Transactional(readOnly = true)
  public String getOrgUserKeyOrThrow(Long orgId) {
    return orgApiCredentialRepository.findTopByOrganization_IdOrderByIdDesc(orgId)
        .map(OrgApiCredential::getUserKey)
        .orElseThrow(() -> new NoSuchElementException("org userKey not found: orgId=" + orgId));
  }

  /** 조직에 userKey를 저장/갱신 (관리자/시드에서 호출) */
  @Transactional
  public OrgApiCredential upsertOrgUserKey(Long orgId, String userKey) {
    Organization org = organizationRepository.findByIdOrThrow(orgId);
    OrgApiCredential cred = orgApiCredentialRepository.findTopByOrganization_IdOrderByIdDesc(orgId)
        .orElseGet(OrgApiCredential::new);
    cred.setOrganization(org);
    cred.setUserKey(userKey);
    return orgApiCredentialRepository.save(cred);
  }

}
