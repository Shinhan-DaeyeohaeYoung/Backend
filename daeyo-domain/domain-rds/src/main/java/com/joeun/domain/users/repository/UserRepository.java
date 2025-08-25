package com.joeun.domain.users.repository;

import com.joeun.domain.users.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUniversity_IdAndStudentId(Long universityId, String studentId);
  // boolean existsByUniversity_IdAndStudentId(Long universityId, String studentId);


}
