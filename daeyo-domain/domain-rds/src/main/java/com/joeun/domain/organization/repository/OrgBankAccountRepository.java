package com.joeun.domain.organization.repository;

import com.joeun.domain.organization.entity.OrgBankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgBankAccountRepository extends JpaRepository<OrgBankAccount, Long> {
  List<OrgBankAccount> findAllByOrganizationId(Long organizationId);

  Optional<OrgBankAccount> findFirstByOrganizationIdAndIsPrimaryTrue(Long organizationId);

  Optional<OrgBankAccount> findByIdAndOrganizationId(Long id, Long organizationId);
}
