package com.joeun.service.user;

import com.joeun.domain.students.entity.Student;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityPointRepository;
import com.joeun.domain.university.repository.UniversityRepository;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserApiCredential;
import com.joeun.domain.users.entity.UserBankAccount;
import com.joeun.domain.users.repository.UserApiCredentialRepository;
import com.joeun.domain.users.repository.UserBankAccountRepository;
import com.joeun.domain.users.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
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
  private final UserApiCredentialRepository credRepo;

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

  // USER CREDENTIAL
  public void saveOrUpdateKey(Long userId, String apiKey) {
    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));

    UserApiCredential cred = credRepo.findByUser_Id(userId)
        .orElseGet(() -> {
          UserApiCredential c = new UserApiCredential();
          c.setUser(user);
          return c;
        });

    boolean changed = !Objects.equals(cred.getApiKey(), apiKey);
    cred.setApiKey(apiKey);
    if (changed) cred.setRotatedAt(LocalDateTime.now());

    credRepo.save(cred);
  }

  @Transactional(readOnly = true)
  public String getKeyOrThrow(Long userId) {
    return credRepo.findByUser_Id(userId)
        .map(UserApiCredential::getApiKey)
        .orElseThrow(() -> new IllegalStateException("credential not found for user: " + userId));
  }

  @Transactional(readOnly = true)
  public boolean hasKey(Long userId) {
    return credRepo.existsByUser_Id(userId);
  }

  @Transactional(readOnly = true)
  public String getUserKeyOrThrow(Long userId) {
    // 여러 개 저장될 수 있다면 "가장 최신" 기준으로 조회
    return credRepo.findTopByUser_IdOrderByIdDesc(userId)
        .map(cred -> cred.getApiKey())
        .orElseThrow(() -> new NoSuchElementException("userKey not found for userId=" + userId));
  }

  @Transactional(readOnly = true)
  public Optional<UserBankAccount> findPrimaryBankAccount(Long userId) {
    return accountRepo.findByUser_IdAndIsPrimaryTrue(userId);
    // 필드명이 isPrimary면: return userBankAccountRepository.findByUser_IdAndIsPrimaryTrue(userId);
  }

}
