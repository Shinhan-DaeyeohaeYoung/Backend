package com.joeun.domain.users.repository;

import com.joeun.domain.organization.types.OrganizationType;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.domain.users.types.UserOrgRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserOrgMembershipRepository extends JpaRepository<UserOrgMembership, Long> {
  boolean existsByUserIdAndOrganizationId(Long userId, Long organizationId);

  Optional<UserOrgMembership> findByUserIdAndOrganizationId(Long userId, Long organizationId);

  @Query("""
      select m
      from UserOrgMembership m
      join fetch m.organization o
      left join fetch o.university u
      left join fetch o.parent p
      where m.id.userId = :userId
      """)
  List<UserOrgMembership> findAllWithOrganizationByUserId(@Param("userId") Long userId);

  @Query("""
      select m
      from UserOrgMembership m
      join fetch m.organization o
      left join fetch o.university u
      left join fetch o.parent p
      where m.id.userId = :userId
        and o.type = :type
      """)
  List<UserOrgMembership> findAllWithOrganizationByUserIdAndType(Long userId, OrganizationType type);

  boolean existsByUserIdAndOrganizationIdAndRole(Long userId, Long orgId, UserOrgRole userOrgRole);
}
