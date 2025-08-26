package com.joeun.service.organization;

import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.repository.OrganizationRepository;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityRepository;
import com.joeun.domain.users.repository.UserOrgMembershipRepository;
import com.joeun.domain.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationDomainService {

  private final OrganizationRepository organizationRepository;

  public Organization create(Organization org) {
    return organizationRepository.save(org);
  }
}
