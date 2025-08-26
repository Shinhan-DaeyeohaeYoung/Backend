package com.joeun.api.organization.service;

import com.google.zxing.NotFoundException;
import com.joeun.api.organization.dto.OrganizationCreateRequest;
import com.joeun.api.organization.dto.OrganizationResponse;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.repository.OrganizationRepository;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityRepository;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.repository.UserOrgMembershipRepository;
import com.joeun.domain.users.repository.UserRepository;
import com.joeun.domain.users.types.UserOrgRole;
import com.joeun.global.config.LoginUser;
import com.joeun.service.organization.OrganizationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationDomainService domainService;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final UniversityRepository universityRepository;
  private final UserOrgMembershipRepository membershipRepository;


  public Organization createOrganization(Long userId, OrganizationCreateRequest req) {
    University university = universityRepository.findById(req.getUniversityId())
        .orElseThrow(() -> new NotFoundException("University not found"));

    Organization parent = organizationRepository.findById(req.getParentOrganizationId())
        .orElseThrow(() -> new NotFoundException("Parent organization not found"));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    // 조직 엔티티 생성
    Organization organization = Organization.builder()
        .name(req.getName())
        .university(university)
        .parent(parent)
        .type(req.getType())
        .isActive(true)
        .build();

    // 도메인 서비스 호출
    Organization saved = domainService.create(organization);

    // Membership 저장
    UserOrgMembership membership = UserOrgMembership.of(user, saved, UserOrgRole.ORG_ADMIN);
    membershipRepository.save(membership);

    return saved;
  }

}
