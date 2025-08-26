package com.joeun.domain.users.repository;

import com.joeun.domain.users.entity.UserOrgMembership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOrgMembershipRepository extends JpaRepository<UserOrgMembership, Long> {

}
