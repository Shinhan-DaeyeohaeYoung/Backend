package com.joeun.service.university;

import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.entity.UniversityPoint;
import com.joeun.domain.university.repository.UniversityPointRepository;
import com.joeun.domain.university.repository.UniversityRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UniversityDomainService {

  private final UniversityRepository univRepo;
  private final UniversityPointRepository univPointRepo;

  public Page<University> search(String q, Pageable pageable) {
    if (StringUtils.hasText(q)) {
      return univRepo.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q, pageable);
    }
    return univRepo.findAll(pageable);
  }

  @Transactional
  public Optional<UniversityPoint> getByUniversityId(Long universityId) {
    return univPointRepo.findByUniversityId(universityId);
  }

  @Transactional
  public List<UniversityPoint> findTop10ByPoint() {
    return univPointRepo.findTop10ByOrderByPointDesc();
  }

  @Transactional(readOnly = true)
  public int getRankByUniversityId(Long universityId) {
    Integer rank = univPointRepo.findRankByUniversityId(universityId); // native 쿼리
    return (rank != null) ? rank : 0;
  }

}
