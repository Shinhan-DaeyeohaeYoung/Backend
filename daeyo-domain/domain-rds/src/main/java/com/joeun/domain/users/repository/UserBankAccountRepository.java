package com.joeun.domain.users.repository;

import com.joeun.domain.users.entity.UserBankAccount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserBankAccountRepository extends JpaRepository<UserBankAccount, Long> {
  long countByUser_Id(Long userId);

  Optional<UserBankAccount> findByUser_IdAndIsPrimaryTrue(Long userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("update UserBankAccount a set a.isPrimary = false " +
      "where a.user.id = :userId and a.isPrimary = true")
  int clearPrimaryForUser(@Param("userId") Long userId);

  List<UserBankAccount> findAllByUser_IdOrderByIsPrimaryDescCreatedAtDesc(Long userId);

}
