package com.joeun.service.deposit;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.repository.DepositRepository;
import com.joeun.domain.deposit.types.DepositStatus;
import com.joeun.domain.organization.entity.OrgBankAccount;
import com.joeun.domain.organization.entity.Organization;
import com.joeun.domain.organization.repository.OrgBankAccountRepository;
import com.joeun.domain.organization.repository.OrganizationRepository;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserBankAccount;
import com.joeun.domain.users.repository.UserBankAccountRepository;
import com.joeun.domain.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepositDomainService {

  private final UserRepository userRepo;
  private final OrganizationRepository orgRepo;
  private final DepositRepository depoRepo;
  private final UserBankAccountRepository userBankAccountRepository;
  private final OrgBankAccountRepository orgBankAccountRepo;

  public Page<Deposit> searchUserDepositsSimple(
      Long userId,
      DepositStatus status,
      Pageable pageable
  ) {
    return depoRepo.findByUserIdAndStatus(userId, status, pageable);
  }

  @Transactional
  public List<Deposit> findByOrganization(Long orgId,
      boolean hasStatuses,
      Collection<DepositStatus> statuses) {
    return depoRepo.findByOrganization(orgId, hasStatuses, statuses);
  }

  @Transactional
  public Deposit createDeposit(Long userId,
      Long organizationId,
      BigDecimal amount,
      Long rentalId /* 더 이상 사용하지 않지만 시그니처 깔끔히 하려면 제거 가능 */,
      Long assertedUniversityId /*nullable*/) {

    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be > 0");
    }

    Organization org = orgRepo.findById(organizationId)
        .orElseThrow(() -> new IllegalStateException("organization not found: " + organizationId));
    University univ = org.getUniversity();
    Long univId = (univ != null ? univ.getId() : null);

    if (assertedUniversityId != null && !assertedUniversityId.equals(univId)) {
      throw new IllegalStateException("university_id mismatch with organization");
    }

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalStateException("user not found: " + userId));

    if (user.getUniversity() == null || !user.getUniversity().getId().equals(univId)) {
      throw new IllegalStateException("user and organization tenant mismatch");
    }

    UserBankAccount userPrimary = userBankAccountRepository
        .findFirstByUserIdAndIsPrimaryTrue(userId)
        .orElse(null);

    OrgBankAccount orgPrimary =
        orgBankAccountRepo.findFirstByOrganizationIdAndIsPrimaryTrue(organizationId)
            .orElseThrow(() -> new IllegalStateException("no primary org bank account"));

    Deposit d = new Deposit();
    d.setUniversity(univ);
    d.setOrganization(org);
    d.setUser(user);
    d.setAmount(amount);
    d.setStatus(DepositStatus.HELD);
    d.setRefundAccount(userPrimary);
    d.setOrgBankAccount(orgPrimary);

    return depoRepo.save(d);
  }

  @Transactional
  public Deposit getByIdOrThrow(Long id) {
    return depoRepo.findById(id)
        .orElseThrow(() -> new NoSuchElementException("deposit not found: " + id));
  }

  @Transactional
  public Deposit getByIdWithAllJoinsOrThrow(Long id) {
    return depoRepo.findByIdWithAllJoins(id)
        .orElseThrow(() -> new NoSuchElementException("deposit not found: " + id));
  }

  @Transactional
  public Deposit refundFull(Deposit deposit, Long actorUserId) {
    if (deposit.getStatus() != DepositStatus.HELD) {
      throw new IllegalStateException("invalid transition: " + deposit.getStatus() + " -> RELEASED");
    }
    if (deposit.getRefundAccount() == null) {
      throw new IllegalStateException("refund account not set");
    }

    deposit.setStatus(DepositStatus.RELEASED);
    // updatedAt 은 @UpdateTimestamp 등에 의해 자동 갱신된다고 가정
    return depoRepo.save(deposit);
  }

  @Transactional
  public Deposit forfeitFull(Deposit deposit, Long actorUserId) {
    if (deposit.getStatus() != DepositStatus.HELD) {
      throw new IllegalStateException("invalid transition: " + deposit.getStatus() + " -> FORFEITED");
    }
    deposit.setStatus(DepositStatus.FORFEITED);
    return depoRepo.save(deposit);
  }

}
