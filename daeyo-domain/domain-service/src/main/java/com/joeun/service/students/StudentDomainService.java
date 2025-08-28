package com.joeun.service.students;

import com.joeun.domain.students.entity.Student;
import com.joeun.domain.students.repository.StudentRepository;
import com.joeun.domain.students.types.SignupStatus;
import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentDomainService {

  private final StudentRepository studentRepository;
  private final UniversityRepository univRepo;

  @Transactional
  public Optional<Student> getByUniversityAndStudentNo(Long universityId, String studentNo) {
    return studentRepository.findByUniversityIdAndStudentNo(universityId, studentNo);
  }

  @Transactional
  public boolean existsByUniversityIdAndStudentNo(Long universityId, String studentNo) {
    return studentRepository.existsByUniversityIdAndStudentNo(universityId, studentNo);
  }

  @Transactional
  public Student create(Long universityId, String studentNo, String name, String email,
      String passwordHash, SignupStatus status) {

    University univRef = univRepo.getReferenceById(universityId);

    Student s = Student.builder()
        .university(univRef)
        .studentNo(studentNo)
        .name(name)
        .email(email)
        .passwordHash(passwordHash)
        .signupStatus(status)
        .build();
    return studentRepository.save(s);
  }

  @Transactional
  public Optional<Student> findByUniversityAndStudentNo(Long universityId, String studentNo) {
    return studentRepository.findByUniversityIdAndStudentNo(universityId, studentNo);
  }

  @Transactional
  public void markRegistered(Student s) {
    s.setSignupStatus(SignupStatus.REGISTERED);
    studentRepository.save(s);
  }

}
