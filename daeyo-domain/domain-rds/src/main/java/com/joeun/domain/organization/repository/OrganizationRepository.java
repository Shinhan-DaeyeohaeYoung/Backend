package com.joeun.domain.organization.repository;

import com.joeun.domain.organization.entity.Organization;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    @Query("""
        select distinct m.organization.id
        from UserOrgMembership m
        where m.user.id = :userId
    """)
    List<Long> findOrgIdsByUserId(@Param("userId") Long userId);

  default Organization findByIdOrThrow(Long id) {
    return findById(id).orElseThrow(
        () -> new jakarta.persistence.EntityNotFoundException("organization not found: " + id)
    );
  }
           
    @Query("""
    select o.id
        from Organization o
    """)
    List<Long> findAllOrganizationIds();
}
