package com.joeun.domain.users.repository;

import com.joeun.domain.users.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUniversity_IdAndStudentId(Long universityId, String studentId);
  // boolean existsByUniversity_IdAndStudentId(Long universityId, String studentId);

  boolean existsByEmail(String email);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update User u set u.point = u.point + :delta where u.id = :userId")
  int addPoint(@Param("userId") Long userId, @Param("delta") long delta);

  @Query("select u.university.id from User u where u.id = :userId")
  Long findUniversityIdByUserId(@Param("userId") Long userId);
}
