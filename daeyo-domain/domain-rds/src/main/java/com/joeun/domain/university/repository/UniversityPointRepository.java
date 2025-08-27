package com.joeun.domain.university.repository;

import com.joeun.domain.university.entity.UniversityPoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityPointRepository extends JpaRepository<UniversityPoint, Long> {
  Optional<UniversityPoint> findByUniversityId(Long universityId);
  List<UniversityPoint> findTop10ByOrderByPointDesc();
}
