package com.joeun.domain.students.repository;

import com.joeun.domain.students.entity.Student;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
  Optional<Student> findByUniversityIdAndStudentNo(Long universityId, String studentNo);

  boolean existsByUniversityIdAndStudentNo(Long universityId, String studentNo);

  
}
