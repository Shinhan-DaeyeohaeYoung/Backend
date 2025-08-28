package com.joeun.domain.organization.repository;

import com.joeun.domain.organization.entity.Organization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
