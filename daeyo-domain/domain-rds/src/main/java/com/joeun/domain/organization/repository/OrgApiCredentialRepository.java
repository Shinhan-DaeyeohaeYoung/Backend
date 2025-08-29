package com.joeun.domain.organization.repository;

import com.joeun.domain.organization.entity.OrgApiCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgApiCredentialRepository extends JpaRepository<OrgApiCredential, Long> {

  Optional<OrgApiCredential> findTopByOrganization_IdOrderByIdDesc(Long orgId);

  boolean existsByOrganization_Id(Long orgId);
}