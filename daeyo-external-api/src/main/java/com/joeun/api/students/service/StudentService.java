package com.joeun.api.students.service;

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
import com.joeun.service.students.AffiliationDomainService;
import com.joeun.service.students.StudentDomainService;
import com.joeun.service.user.UserDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

  private final UserDomainService userDomainService;
  private final StudentDomainService studentDomainService;
  private final PasswordEncoder passwordEncoder;
  private final AffiliationDomainService affiliationDomainService;

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

    // 5) affiliation 이관 → membership
    List<StudentOrgAffiliation> affs = affiliationDomainService.findAllByStudentId(s.getId());
    List<UserOrgMembership> memberships = affiliationDomainService.migrateToUserMemberships(u, affs);

    // 6) 학생 상태 변경
    studentDomainService.markRegistered(s);

    // 7) 응답 구성
    var userDto = new StudentSignupResponse.UserDto(
        u.getId(), u.getUniversity().getId(), u.getName(), u.getEmail(), u.getStudentId(), u.getRole()
    );
    var memDtos = memberships.stream()
        .map(m -> new UserMembershipDto(m.getOrganization().getId(), m.getRole().name()))
        .toList();

    return new StudentSignupResponse(userDto, memDtos);
  }

}
