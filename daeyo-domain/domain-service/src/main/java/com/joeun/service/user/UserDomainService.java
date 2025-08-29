package com.joeun.service.user;

import com.joeun.domain.students.entity.Student;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityPointRepository;
import com.joeun.domain.university.repository.UniversityRepository;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserBankAccount;
import com.joeun.domain.users.repository.UserBankAccountRepository;
import com.joeun.domain.users.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class  UserDomainService {

  private final UserRepository userRepo;
  private final UniversityRepository univRepo;
  private final UniversityPointRepository univPointRepo;
  private final UserBankAccountRepository accountRepo;

  public User createUser(User user){
    return userRepo.save(user);
  }

  public University getUniversity(Long univ_id){
    return univRepo.getReferenceById(univ_id);
  }

  public Optional<User> findByUnivAndStudentId(Long universityId, String studentId) {
    return userRepo.findByUniversity_IdAndStudentId(universityId, studentId);
  }

  public Optional<User> findById(Long userId) {return userRepo.findById(userId);}

  @Transactional
  public UserBankAccount addBankAccountPrepared(
      User user,
      String bankCode,
      String bankName,
      String holderName,
      String accountNoMasked,
      byte[] accountNoEncrypted,
      Boolean primaryFlag
  ) {
    boolean makePrimary = accountRepo.countByUser_Id(user.getId()) == 0
        || Boolean.TRUE.equals(primaryFlag);

    if (makePrimary) {
      accountRepo.clearPrimaryForUser(user.getId());
    }

    UserBankAccount acc = new UserBankAccount();
    acc.setUser(user);
    acc.setBankCode(bankCode);
    acc.setBankName(bankName);
    acc.setAccountHolderName(holderName);
    acc.setAccountNoMasked(accountNoMasked);
    acc.setAccountNo(accountNoEncrypted);
    acc.setPrimary(makePrimary);

    return accountRepo.save(acc);
  }

  @Transactional(readOnly = true)
  public List<UserBankAccount> listBankAccounts(Long userId) {
    return accountRepo.findAllByUser_IdOrderByIsPrimaryDescCreatedAtDesc(userId);
  }

  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return userRepo.existsByEmail(email);
  }

  @Transactional
  public User createFromStudent(Student s) {
    User u = User.builder()
        .university(s.getUniversity())
        .name(s.getName())
        .email(s.getEmail())
        .studentId(s.getStudentNo())     // User.studentId = 학번 문자열
        .passwordHash(s.getPasswordHash()) // 동일 해시 복사
        .role("USER")
        .build();
    return userRepo.save(u);
  }

  @Transactional
  public void addOnRefund(Long userId) {
    // 1) 사용자 포인트 +100 (원자적 증분)
    int updated = userRepo.addPoint(userId, 100L);
    if (updated == 0) {
      throw new IllegalStateException("User not found or point update failed: " + userId);
    }

    // 2) 해당 사용자의 대학 식별
    Long univId = userRepo.findUniversityIdByUserId(userId);
    if (univId == null) {
      throw new IllegalStateException("University not found for user: " + userId);
    }

    // 3) 대학 포인트 +100 (UPSERT 원자적 증분)
    univPointRepo.upsertAdd(univId, 100L);
  }


}
