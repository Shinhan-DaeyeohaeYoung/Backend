package com.joeun.service.organization;

import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.repository.OrganizationRepository;
import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.repository.UserOrgMembershipRepository;
import com.joeun.domain.users.types.UserOrgRole;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationDomainService {

  private final OrganizationRepository organizationRepository;
  private final UserOrgMembershipRepository membershipRepository;

  public Organization getOrganization(Long id) {
    return organizationRepository.findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Organization not found"));
  }

  @Transactional
  public void addMemberCascade(User user, Organization startOrg) {
    Set<Long> visited = new HashSet<>();
    Organization curr = startOrg;

    while (curr != null && visited.add(curr.getId())) {
      if (!membershipRepository.existsByUserIdAndOrganizationId(user.getId(), curr.getId())) {
        UserOrgMembership m = UserOrgMembership.of(user, curr, UserOrgRole.ORG_MEMBER);
        membershipRepository.save(m);
      }

      curr = curr.getParent();
    }
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
}
