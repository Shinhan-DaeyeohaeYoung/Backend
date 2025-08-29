package com.joeun.api.user.service;

import com.joeun.api.exception.UnauthorizedException;
import com.joeun.api.ssafyAPI.client.SsafyDemandDepositClient;
import com.joeun.api.ssafyAPI.client.SsafyMemberClient;
import com.joeun.api.ssafyAPI.dto.AccountApiHeader;
import com.joeun.api.ssafyAPI.dto.InquireDemandDepositAccountBalanceRequest;
import com.joeun.api.ssafyAPI.dto.MemberCreateRequest;
import com.joeun.api.ssafyAPI.dto.MemberCreateResponse;
import com.joeun.api.user.dto.MyBalanceResponse;
import com.joeun.api.user.dto.UserBankAccountCreateRequest;
import com.joeun.api.user.dto.UserBankAccountCreateResponse;
import com.joeun.api.user.dto.UserBankAccountResponse;
import com.joeun.api.user.dto.UserSigninRequest;
import com.joeun.api.user.dto.UserSigninResponse;
import com.joeun.api.user.dto.UserSignupRequest;
import com.joeun.domain.users.entity.User;

import com.joeun.domain.university.entity.University;
import com.joeun.global.config.LoginUser;
import com.joeun.global.util.AccountCipher;
import com.joeun.global.util.JwtUtil;
import com.joeun.service.user.UserDomainService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserDomainService userDomainService;
  private final PasswordEncoder encoder;
  private final JwtUtil jwtUtil;
  private final AccountCipher accountCipher;

  private final SsafyMemberClient ssafyMemberClient;
  private final SsafyDemandDepositClient ssafyDemandDepositClient;


  @Value("${ssafy.api-key}")
  private String ssafyAdminApiKey;

/*  public void createUser(UserSignupRequest req) {
    University univ = userDomainService.getUniversity(req.getUniversityId());

    User user = User.builder()
        .university(univ)
        .name(req.getName())
        .email(req.getEmail())
        .studentId(req.getStudentId())
        .passwordHash(encoder.encode(req.getPassword()))
        .role("USER")
        .build();

    User saved = userDomainService.createUser(user);

    String digits = req.getAccountNo().replaceAll("\\D", "");
    String masked = accountCipher.mask(digits);
    byte[] encrypted = accountCipher.encrypt(digits);

    userDomainService.addBankAccountPrepared(
        saved,
        req.getBankCode(),
        req.getBankName(),
        req.getAccountHolderName(),
        masked,
        encrypted,
        req.getPrimary()
    );

    // ✅ SSAFY 회원 생성 호출 → userKey 저장
    //    요청: { apiKey: adminKey, userId: email }
    MemberCreateRequest mReq = new MemberCreateRequest(ssafyAdminApiKey, saved.getEmail());
    MemberCreateResponse mRes = ssafyMemberClient.createMember(mReq);

    log.info("[SSAFY] createMember called for email={}, userKey={}", saved.getEmail(), mRes.getUserKey());
    // 응답 userKey 저장(평문)
    userDomainService.saveOrUpdateKey(saved.getId(), mRes.getUserKey());
  }*/

  public UserSigninResponse login(UserSigninRequest req){
    // 1) 사용자 조회 (학교+학번)
    User user = userDomainService.findByUnivAndStudentId(req.getUniversityId(), req.getStudentId())
        .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    // 2) 비밀번호 검증
    if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    // 3) 토큰 발급
    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    // 4) 응답
    return new UserSigninResponse(accessToken, refreshToken, user.getName());
  }

  public List<UserBankAccountResponse> listMyBankAccounts(Long id) {
    return userDomainService.listBankAccounts(id).stream()
        .map(UserBankAccountResponse::from)
        .toList();
  }

  public UserBankAccountCreateResponse addMyBankAccount(Long userId, UserBankAccountCreateRequest req) {
    User user = userDomainService.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 방어적 정규화
    String digits = req.getAccountNo().replaceAll("\\D", "");
    String masked = accountCipher.mask(digits);
    byte[] encrypted = accountCipher.encrypt(digits);

    // 요구사항: 본 API에서는 대표계좌로 만들지 않음 → 항상 false
    var saved = userDomainService.addBankAccountPrepared(
        user,
        req.getBankCode(),
        req.getBankName(),
        req.getAccountHolderName(),
        masked,
        encrypted,
        false
    );

    return new UserBankAccountCreateResponse(
        saved.getId(),
        saved.getAccountNoMasked(),
        saved.isPrimary(),
        saved.isVerified()
    );
  }

  // USER CREDENTIALS
  /** 회원가입 직후: 계좌 생성 API 호출 → 받은 key 저장 */
  @Transactional
  public void registerKeyAfterAccountCreated(Long userId, String issuedApiKey) {
    userDomainService.saveOrUpdateKey(userId, issuedApiKey);
  }

  /** 이미 계좌가 있다면: 계좌 조회 API → 받은 key 저장/갱신 */
  @Transactional
  public void syncKeyFromAccountLookup(Long userId, String apiKeyFromLookup) {
    userDomainService.saveOrUpdateKey(userId, apiKeyFromLookup);
  }

  /** 실행자 기준 user key 조회 (컨트롤러/헤더팩토리 등에서 사용) */
  @Transactional
  public String getUserKey(Long executorUserId) {
    return userDomainService.getKeyOrThrow(executorUserId);
  }

  public MyBalanceResponse getMyBalance(@NotNull LoginUser loginUser) {
    Long userId = loginUser.id();

    // 1) userKey 확보
    String userKey = userDomainService.getUserKeyOrThrow(userId);

    // 2) 주계좌 엔티티 확보 (primary=true). 없으면 404
    var primary = userDomainService.findPrimaryBankAccount(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "primary bank account not found"));

    // 3) 평문 계좌번호 복구 (encrypt만 되어 저장되어 있으므로 decrypt 필요)
    // decrypt 메서드명은 프로젝트 구현에 맞춰 조정하세요.
    byte[] enc = primary.getAccountNo();
    if (enc == null || enc.length == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "encrypted account not found");
    }
    String accountNoPlain = accountCipher.decrypt(enc);
    // 4) 외부 API 호출 요청 빌드
    AccountApiHeader header = AccountApiHeader.builder()
        .apiName("inquireDemandDepositAccountBalance")
        .transmissionDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        .transmissionTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss")))
        .institutionCode("00100")
        .fintechAppNo("001")
        .apiServiceCode("inquireDemandDepositAccountBalance")
        .institutionTransactionUniqueNo(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100000, 999999))
        .apiKey(ssafyAdminApiKey)
        .userKey(userKey)
        .build();

    var req = InquireDemandDepositAccountBalanceRequest.builder()
        .header(header)
        .accountNo(accountNoPlain)
        .build();

    // 5) 호출
    var res = ssafyDemandDepositClient.inquireDemandDepositAccountBalance(req);

    // 6) 응답 코드 검증
    if (res == null || res.getHeader() == null || !"H0000".equals(res.getHeader().getResponseCode())) {
      String msg = (res != null && res.getHeader() != null) ? res.getHeader().getResponseMessage() : "upstream error";
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "balance inquiry failed: " + msg);
    }

    // 7) 잔액 파싱 (외부는 문자열)
    String balanceStr = res.getRec() != null ? res.getRec().getAccountBalance() : null;
    if (balanceStr == null) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "missing accountBalance in response");
    }

    BigDecimal balance;
    try {
      balance = new BigDecimal(balanceStr);
    } catch (NumberFormatException nfe) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "invalid accountBalance format");
    }

    return new MyBalanceResponse(balance);
  }
}
