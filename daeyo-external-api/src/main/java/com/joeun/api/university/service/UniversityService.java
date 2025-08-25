package com.joeun.api.university.service;

import com.joeun.api.university.dto.UniversityResponse;
import com.joeun.service.university.UniversityDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityDomainService univDomainService;

    public Page<UniversityResponse> search(String q, Pageable pageable) {
        return univDomainService.search(q, pageable).map(UniversityResponse::from);
    }

    public List<UniversityResponse> searchList(String q, Pageable pageable) {
        return search(q, pageable).getContent();
    }

}
