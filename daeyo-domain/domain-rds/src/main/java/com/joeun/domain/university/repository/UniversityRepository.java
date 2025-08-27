package com.joeun.domain.university.repository;

import com.joeun.domain.university.entity.University;
import com.joeun.domain.users.entity.UserOrgMembership;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {
  Page<University> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code, Pageable pageable);

  Optional<University> findById(Long universityId);

}
