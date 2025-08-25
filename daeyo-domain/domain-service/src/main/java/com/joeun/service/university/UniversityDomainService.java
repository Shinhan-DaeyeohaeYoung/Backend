package com.joeun.service.university;

import com.joeun.domain.university.entity.University;
import com.joeun.domain.university.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UniversityDomainService {

  private final UniversityRepository univRepo;

  public Page<University> search(String q, Pageable pageable) {
    if (StringUtils.hasText(q)) {
      return univRepo.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q, pageable);
    }
    return univRepo.findAll(pageable);
  }
}
