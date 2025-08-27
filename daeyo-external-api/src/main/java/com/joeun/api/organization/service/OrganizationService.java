package com.joeun.api.organization.service;

import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.global.config.LoginUser;
import com.joeun.service.organization.OrganizationDomainService;
import com.joeun.service.user.UserDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

  private final OrganizationDomainService organizationDomainService;
  private final UserDomainService userDomainService;

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

}