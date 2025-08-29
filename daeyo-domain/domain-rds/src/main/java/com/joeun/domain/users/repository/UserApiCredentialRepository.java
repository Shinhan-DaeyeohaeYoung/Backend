package com.joeun.domain.users.repository;

import com.joeun.domain.users.entity.UserApiCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserApiCredentialRepository extends JpaRepository<UserApiCredential, Long> {
  Optional<UserApiCredential> findByUser_Id(Long userId);
  boolean existsByUser_Id(Long userId);

  Optional<UserApiCredential> findTopByUser_IdOrderByIdDesc(Long userId);

}