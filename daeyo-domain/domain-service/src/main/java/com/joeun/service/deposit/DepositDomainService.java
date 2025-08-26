package com.joeun.service.deposit;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.repository.DepositRepository;
import com.joeun.domain.deposit.types.DepositStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositDomainService {

  private final DepositRepository depoRepo;

  public Page<Deposit> searchUserDepositsSimple(
      Long userId,
      DepositStatus status,
      Pageable pageable
  ) {
    return depoRepo.findByUserIdAndStatus(userId, status, pageable);
  }
}
