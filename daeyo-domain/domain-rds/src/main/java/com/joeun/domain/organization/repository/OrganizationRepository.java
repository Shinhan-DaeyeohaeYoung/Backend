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
        /* 필요하면 활성 조건 추가
           and m.active = true
           and m.organization.active = true
        */
    """)
    List<Long> findOrgIdsByUserId(@Param("userId") Long userId);

}
