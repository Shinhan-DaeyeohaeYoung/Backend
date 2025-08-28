package com.joeun.domain.students.repository;

import com.joeun.domain.students.entity.StudentOrgAffiliation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationRepository extends JpaRepository<StudentOrgAffiliation, Long> {
  List<StudentOrgAffiliation> findAllByStudent_Id(Long studentId);
}
