package com.joeun.api.students.service;

import com.joeun.api.ssafyAPI.client.SsafyDemandDepositClient;
import com.joeun.api.ssafyAPI.client.SsafyMemberClient;
import com.joeun.api.ssafyAPI.dto.AccountApiHeader;
import com.joeun.api.ssafyAPI.dto.CreateDemandDepositAccountRequest;
import com.joeun.api.ssafyAPI.dto.MemberCreateRequest;
import com.joeun.api.ssafyAPI.dto.MemberCreateResponse;
import com.joeun.api.ssafyAPI.dto.UpdateDemandDepositAccountDepositRequest;
import com.joeun.api.students.dto.StudentCreateRequest;
import com.joeun.api.students.dto.StudentResponse;
import com.joeun.api.students.dto.StudentSignupRequest;
import com.joeun.api.students.dto.StudentSignupResponse;
import com.joeun.api.students.dto.StudentVerifyRequest;
import com.joeun.api.students.dto.UserMembershipDto;
import com.joeun.domain.students.entity.Student;
import com.joeun.domain.students.entity.StudentOrgAffiliation;
import com.joeun.domain.students.types.SignupStatus;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.users.entity.UserOrgMembership;
import com.joeun.global.util.AccountCipher;
import com.joeun.service.students.AffiliationDomainService;
import com.joeun.service.students.StudentDomainService;
import com.joeun.service.user.UserDomainService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentService {

  private final AccountCipher accountCipher;
  private final UserDomainService userDomainService;
  private final StudentDomainService studentDomainService;
  private final PasswordEncoder passwordEncoder;
  private final AffiliationDomainService affiliationDomainService;

  private final SsafyMemberClient ssafyMemberClient;
  private final SsafyDemandDepositClient ssafyDemandDepositClient;
  private final UserDomainService userCredentialDomainService;

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");
  private static final DateTimeFormatter TS_FMT   = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  @Value("${ssafy.api-key}")
  private String ssafyMemberAdminApiKey;

  @Value("${ssafy.account-type}")
  private String ssafyAccountTypeUniqueNo;

  public StudentResponse create(StudentCreateRequest req) {
    // 중복 확인
    boolean exists = studentDomainService.existsByUniversityIdAndStudentNo(req.universityId(), req.studentNo());
    if (exists) throw new ResponseStatusException(HttpStatus.CONFLICT, "student already exists");

    String hash = passwordEncoder.encode(req.password());

    Student s = studentDomainService.create(
        req.universityId(), req.studentNo(), req.name(), req.email(), hash, SignupStatus.PENDING
    );

    return new StudentResponse(
        s.getId(),
        s.getUniversity().getId(),
        s.getStudentNo(),
        s.getName(),
        s.getEmail(),
        s.getSignupStatus().name()
    );
  }

  public StudentResponse verifyAndFetch(StudentVerifyRequest req) {
    // 1) 도메인에서 학생 로드 (없으면 도메인 예외 발생)
    Student s = studentDomainService
        .getByUniversityAndStudentNo(req.universityId(), req.studentNo())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

    // 2) 가입 상태 확인 (도메인 규칙)
    if (s.getSignupStatus() != SignupStatus.PENDING) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "student already registered");
    }

    // 3) 비밀번호 검증 (인프라 의존 → 앱 계층에서 처리)
    if (!passwordEncoder.matches(req.password(), s.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
    }

    // 4) 응답 DTO 매핑
    return new StudentResponse(
        s.getId(),
        s.getUniversity().getId(),
        s.getStudentNo(),
        s.getName(),
        s.getEmail(),
        s.getSignupStatus().name()
    );
  }

  public StudentSignupResponse signup(StudentSignupRequest req) {
    // 1) 학생 로드
    Student s = studentDomainService.findByUniversityAndStudentNo(req.universityId(), req.studentNo())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found"));

    // 2) 상태/비번 검증
    if (s.getSignupStatus() != SignupStatus.PENDING)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "student already registered");

    if (!passwordEncoder.matches(req.password(), s.getPasswordHash()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");

    // 3) email 중복 방지
    if (userDomainService.existsByEmail(s.getEmail()))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "email already used");

    // 4) User 생성
    User u = userDomainService.createFromStudent(s); // name/email/university/studentNo/passwordHash 복사

    // 5.9) affiliation 이관 → membership
    List<StudentOrgAffiliation> affs = affiliationDomainService.findAllByStudentId(s.getId());
    List<UserOrgMembership> memberships = affiliationDomainService.migrateToUserMemberships(u, affs);

/* 기존에 계좌번호를 입력받던 부분을 없애고, API 응답 값으로 대체

    String digitsOnly = req.accountNo().replaceAll("\\D", "");
    if (digitsOnly.length() < 6 || digitsOnly.length() > 30) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid account number length");
    }

    String masked = accountCipher.mask(digitsOnly);
    byte[] encrypted = accountCipher.encrypt(digitsOnly);

    // 첫 계좌는 자동 primary, 혹은 요청에서 명시(primaryFlag) 가능하도록
    Boolean primaryFlag = null; // 필요하면 req에서 받아와 세팅
    userDomainService.addBankAccountPrepared(
        u,
        req.bankCode(),
        req.bankName(),          // nullable OK
        req.accountHolderName(),
        masked,
        encrypted,
        primaryFlag
    );*/

    // API
    // SSAFY 회원 생성 호출
    String email = u.getEmail();
    MemberCreateRequest mReq = new MemberCreateRequest(ssafyMemberAdminApiKey, email);
    MemberCreateResponse mRes = ssafyMemberClient.createMember(mReq);

    // userKey 저장
    userCredentialDomainService.saveOrUpdateKey(u.getId(), mRes.getUserKey());
    StudentSignupResponse.BankAccountDto bankDto = null;
    // SSAFY 계좌 개설 호출 (userKey 필요)
    try {
      // 제품코드는 설정값을 권장 (ex. application.yml: ssafy.accountTypeUniqueNo)
      String productNo = ssafyAccountTypeUniqueNo; // @Value 주입 또는 설정 빈
      ZonedDateTime nowKst = ZonedDateTime.now(KST);
      String date = nowKst.format(DATE_FMT);
      String time = nowKst.format(TIME_FMT);
      String idem = nowKst.format(TS_FMT) + ThreadLocalRandom.current().nextInt(100000, 999999);

      var accRes = ssafyDemandDepositClient.createDemandDepositAccount(
          CreateDemandDepositAccountRequest.builder()
              .header(AccountApiHeader.builder()
                  .apiName("createDemandDepositAccount")
                  .transmissionDate(date)               // KST 날짜
                  .transmissionTime(time)               // KST 시간
                  .institutionCode("00100")
                  .fintechAppNo("001")
                  .apiServiceCode("createDemandDepositAccount")
                  .institutionTransactionUniqueNo(idem) // 동일 now 기반 멱등키
                  .apiKey(ssafyMemberAdminApiKey)
                  .userKey(mRes.getUserKey())           // ✅ 필수
                  .build())
              .accountTypeUniqueNo(productNo)
              .build()
      );

      // 응답에서 계좌번호/은행코드 추출
            String extAccountNo = accRes.getRec().getAccountNo();
            String extBankCode  = accRes.getRec().getBankCode();

            if (extAccountNo == null || extAccountNo.isBlank()) {
                throw new IllegalStateException("accountNo missing from SSAFY API response");
              }

            // 저장용 마스킹/암호화
            String maskedFromApi = accountCipher.mask(extAccountNo);
            byte[] encryptedFromApi = accountCipher.encrypt(extAccountNo);

            // 은행명 해석 (유틸/서비스 추가해서 코드→이름 매핑, 없으면 null 허용)
            String bankNameResolved = "신한은행";

            // 예금주명: 사용자 실명 사용(요구사항에 따라 u.getName() 또는 s.getName())
            String holderName = u.getName();

            // ✅ DB 저장: 첫 계좌이므로 무조건 primary = true
            Boolean primaryFlag2 = Boolean.TRUE;
            userDomainService.addBankAccountPrepared(
                u,
                extBankCode,        // API 응답 은행코드
                bankNameResolved,   // 코드→이름 매핑 결과(없으면 null)
                holderName,         // 예금주명
                maskedFromApi,
                encryptedFromApi,
                primaryFlag2        // ← 무조건 true
            );

          bankDto = new StudentSignupResponse.BankAccountDto(
                  maskedFromApi,
                  extBankCode,
                  bankNameResolved,
                  true,   // 항상 primary
                  false   // 최초 isVerified=false
          );

      try {

        // 키에 공백/개행 붙는 사고 방지
        String apiKey = ssafyMemberAdminApiKey.trim();

        var depositHeader = AccountApiHeader.builder()
            .apiName("updateDemandDepositAccountDeposit")
            .transmissionDate(date)           // KST 날짜
            .transmissionTime(time)            // KST 시간
            .institutionCode("00100")
            .fintechAppNo("001")
            .apiServiceCode("updateDemandDepositAccountDeposit")
            .institutionTransactionUniqueNo(idem)
            .apiKey(apiKey)                    // 관리자 키
            .userKey(mRes.getUserKey())        // 방금 받은 사용자 userKey
            .build();

        var depositReq = UpdateDemandDepositAccountDepositRequest.builder()
            .header(depositHeader)
            .accountNo(extAccountNo)           // 방금 개설한 사용자 계좌
            .transactionBalance("100000")      // ← 100,000원
            .transactionSummary("(수시입출금) : 입금")
            .build();

        var depositRes = ssafyDemandDepositClient.updateDemandDepositAccountDeposit(depositReq);
        if (depositRes == null || depositRes.getHeader() == null
            || !"H0000".equals(depositRes.getHeader().getResponseCode())) {
          String msg = (depositRes != null && depositRes.getHeader() != null)
              ? depositRes.getHeader().getResponseMessage() : "upstream error";
          log.warn("Initial deposit failed for userId={}, reason={}", u.getId(), msg);
        } else {
          log.info("Initial deposit success: userId={}, amount=100000, idem={}",
              u.getId(), depositRes.getHeader().getInstitutionTransactionUniqueNo());
        }
      } catch (Exception ex) {
        log.warn("Initial deposit error for userId={}", u.getId(), ex);
      }

    } catch (Exception e) {
      // 계좌 개설 실패해도 회원가입은 진행 (운영정책에 맞게 경고/재시도 큐잉)
      log.warn("SSAFY account open failed for userId={}", u.getId(), e);
    }
    // API 끝

    // 7) 학생 상태 변경
    studentDomainService.markRegistered(s);

    // 8) 응답 구성 (계좌는 마스킹만 노출)
    var userDto = new StudentSignupResponse.UserDto(
        u.getId(), u.getUniversity().getId(), u.getName(), u.getEmail(), u.getStudentId(), u.getRole()
    );
    var memDtos = memberships.stream()
        .map(m -> new UserMembershipDto(m.getOrganization().getId(), m.getRole().name()))
        .toList();

    return new StudentSignupResponse(userDto, memDtos, bankDto);
  }

}
