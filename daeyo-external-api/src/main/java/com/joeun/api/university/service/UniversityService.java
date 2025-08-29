package com.joeun.api.university.service;

import com.joeun.api.university.dto.UniversityPointResponse;
import com.joeun.api.university.dto.UniversityPointTopItem;
import com.joeun.api.university.dto.UniversityPointTopResponse;
import com.joeun.api.university.dto.UniversityResponse;
import com.joeun.domain.university.entity.UniversityPoint;
import com.joeun.domain.users.entity.User;
import com.joeun.global.config.LoginUser;
import com.joeun.service.university.UniversityDomainService;
import com.joeun.service.user.UserDomainService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UniversityService {

    private final UniversityDomainService univDomainService;
    private final UserDomainService userDomainService;

    public Page<UniversityResponse> search(String q, Pageable pageable) {
        return univDomainService.search(q, pageable).map(UniversityResponse::from);
    }

    public List<UniversityResponse> searchList(String q, Pageable pageable) {
        return search(q, pageable).getContent();
    }

    public UniversityResponse getMyUniversity(LoginUser loginUser) {
        User user = userDomainService.findById(loginUser.id())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));

        var univ = user.getUniversity();
        if (univ == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "University not set");
        }

        return UniversityResponse.builder()
            .id(univ.getId())
            .name(univ.getName())
            .code(univ.getCode())
            .createdAt(univ.getCreatedAt())
            .updatedAt(univ.getUpdatedAt())
            .build();
    }
    public UniversityPointResponse getMyUniversityPoints(LoginUser loginUser) {
        User user = userDomainService.findById(loginUser.id())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (user.getUniversity() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User has no university assigned");
        }

        Long univId = user.getUniversity().getId();

        UniversityPoint up = univDomainService.getByUniversityId(univId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "University point not found"));

        int rank = univDomainService.getRankByUniversityId(univId);

        return UniversityPointResponse.builder()
            .university_id(univId)
            .point(up.getPoint())
            .rank(rank)
            .created_at(up.getCreatedAt() != null ? up.getCreatedAt().toString() : null)
            .updated_at(up.getUpdatedAt() != null ? up.getUpdatedAt().toString() : null)
            .build();
    }

    public UniversityPointResponse getUniversityPoints(LoginUser loginUser, Long targetUniversityId) {
        User user = userDomainService.findById(loginUser.id())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "User not found"));

        Long myUnivId = user.getUniversity() != null ? user.getUniversity().getId() : null;

        UniversityPoint up = univDomainService.getByUniversityId(targetUniversityId)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "University point not found"));

        return UniversityPointResponse.builder()
            .university_id(up.getUniversity().getId())
            .point(up.getPoint())
            .updated_at(up.getUpdatedAt() != null ? up.getUpdatedAt().toString() : null)
            .build();
    }

    public UniversityPointTopResponse getTopUniversitiesByPoint() {
        List<UniversityPoint> list = univDomainService.findTop10ByPoint();

        List<UniversityPointTopItem> items = list.stream()
            .map(up -> UniversityPointTopItem.builder()
                .university_id(up.getUniversity().getId())
                .name(up.getUniversity().getName())
                .code(up.getUniversity().getCode())
                .point(up.getPoint())
                .updated_at(up.getUpdatedAt() != null ? up.getUpdatedAt().toString() : null)
                .build())
            .toList();

        return UniversityPointTopResponse.builder()
            .items(items)
            .count(items.size())
            .build();
    }

}
