package com.joeun.api.university.controller;

import com.joeun.api.university.dto.UniversityPointResponse;
import com.joeun.api.university.dto.UniversityPointTopResponse;
import com.joeun.api.university.dto.UniversityResponse;
import com.joeun.api.university.service.UniversityService;
import com.joeun.global.config.LoginUser;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/universities")
@RequiredArgsConstructor
public class UniversityController {

  private final UniversityService univService;

  @GetMapping
  public ResponseEntity<List<UniversityResponse>> list(
      @RequestParam(name = "q", required = false) String q,
      @PageableDefault(size = 20, sort = "name") Pageable pageable
  ) {
    var page = univService.search(q, pageable);
    var items = page.getContent();

    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Page", String.valueOf(pageable.getPageNumber()));
    headers.add("X-Size", String.valueOf(pageable.getPageSize()));
    headers.add("X-Total-Elements", String.valueOf(page.getTotalElements()));
    headers.add("X-Total-Pages", String.valueOf(page.getTotalPages()));
    headers.add("X-Sort", page.getSort().toString());

    return ResponseEntity.ok().headers(headers).body(items);
  }

  @GetMapping("/me")
  public ResponseEntity<UniversityResponse> getMyUniversity(
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    return ResponseEntity.ok(univService.getMyUniversity(loginUser));
  }

  @GetMapping("/me/points")
  public ResponseEntity<UniversityPointResponse> getMyUniversityPoints(
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    return ResponseEntity.ok(univService.getMyUniversityPoints(loginUser));
  }

  @GetMapping("/{id}/points")
  public ResponseEntity<UniversityPointResponse> getUniversityPoints(
      @PathVariable("id") Long universityId,
      @AuthenticationPrincipal LoginUser loginUser
  ) {
    return ResponseEntity.ok(univService.getUniversityPoints(loginUser, universityId));
  }

  @GetMapping("/points/top")
  public ResponseEntity<UniversityPointTopResponse> getTopUniversitiesByPoint() {
    return ResponseEntity.ok(univService.getTopUniversitiesByPoint());
  }
}
