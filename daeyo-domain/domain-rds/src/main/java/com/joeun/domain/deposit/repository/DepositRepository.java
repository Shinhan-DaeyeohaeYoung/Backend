package com.joeun.domain.deposit.repository;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.types.DepositStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

  Page<Deposit> findByUserIdAndStatus(Long userId, DepositStatus status, Pageable pageable);
}
