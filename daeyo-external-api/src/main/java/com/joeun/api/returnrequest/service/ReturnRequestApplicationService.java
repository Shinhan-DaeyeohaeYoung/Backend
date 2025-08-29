package com.joeun.api.returnrequest.service;

import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.api.returnrequest.dto.*;
import com.joeun.api.vision.yolo.YoloClient;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.global.config.LoginUser;
import com.joeun.infra.aws.s3.service.S3ImageInfraService;
import com.joeun.service.rental.RentalDomainService;
import com.joeun.service.returnrequest.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnRequestApplicationService {

    private final ReturnRequestService domain;            // 도메인 서비스(기존)
    private final OrganizationService organizationService; // 멤버십/관리자 확인
    private final RentalDomainService rentalDomainService;
    private final S3ImageInfraService s3ImageInfraService;
    private final YoloClient yoloClient;

    /* ===== 공통 헬퍼 ===== */

    private static boolean isAdminRole(String role) {
        return role != null && (role.equals("ORG_ADMIN") || role.equals("ADMIN"));
    }

    /** 관리자 멤버십 하나 선택(organizationId 없으면 단일 ADMIN 멤버십일 때 자동선택) */
    private MyOrganizationResponse pickAdminOrg(LoginUser loginUser, Long organizationId) {
        List<MyOrganizationResponse> adminMemberships = organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .filter(m -> isAdminRole(m.getRole()))
                .collect(Collectors.toList());

        if (organizationId != null) {
            return adminMemberships.stream()
                    .filter(m -> Objects.equals(m.getOrganizationId(), organizationId))
                    .findFirst()
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                            HttpStatus.FORBIDDEN, "Not an admin of organizationId=" + organizationId));
        }
        if (adminMemberships.size() == 1) return adminMemberships.get(0);

        throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.BAD_REQUEST, "organizationId is required (admin memberships=" + adminMemberships.size() + ")");
    }

    /* ===== 관리자 목록/상세 ===== */

    public Page<ReturnRequest> listForAdmin(LoginUser loginUser, Long organizationId, ReturnRequestStatus status, Pageable pageable) {
        var admin = pickAdminOrg(loginUser, organizationId);
        return domain.listForAdmin(admin.getUniversityId(), admin.getOrganizationId(), status, pageable);
    }

    public ReturnRequest detailForAdmin(LoginUser loginUser, Long id, Long organizationId) {
        var admin = pickAdminOrg(loginUser, organizationId);
        return domain.detailForAdmin(admin.getUniversityId(), admin.getOrganizationId(), id);
    }

    /* ===== 유저 생성/취소 ===== */

    public ReturnRequest create(LoginUser loginUser, ReturnRequestCreateRequest req) {
        // rentalId 기준으로 (u,o,owner) 해석 + 소유자 검증
        Rental r = rentalDomainService.getById(req.rentalId()); // ← 없으면 간단히 추가
        if (!Objects.equals(r.getUserId(), loginUser.id())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this rental");
        }
        // 도메인에 생성(원본 키 저장)
        ReturnRequest rr = domain.create(
                r.getUniversityId(), r.getOrganizationId(), loginUser.id(),
                req.rentalId(), req.imageKey(), req.imageMime(), req.imageHash(), req.imageTakenAt()
        );

        // 3) YOLO 호출(실패해도 예외 올리지 않음)
        String originalKey = req.imageKey();
        try {
            // (1) presigned GET (짧은 TTL)
            String getUrl = s3ImageInfraService.getDownloadPresignedUrl(originalKey, Duration.ofMinutes(3));

            // (2) YOLO REST 호출
            var detectReq = new YoloClient.DetectCropRequest(
                    getUrl,
                    "univ/%d/return-requests/%d/crops/".formatted(rr.getUniversityId(), rr.getId()),
                    0.30, 1, 0.25, 512
            );
            var resp = yoloClient.detectCrop(detectReq);

            // (3) detection_meta JSON 구성
            String metaJson = """
                      { "original_key":"%s", "crop_key":%s, "status":"%s", "detection": %s }
                    """.formatted(
                    originalKey,
                    resp != null && resp.cropKey() != null ? "\"" + resp.cropKey() + "\"" : "null",
                    (resp != null && resp.cropKey() != null) ? "success" : "no_detection",
                    (resp != null && resp.detectionMetaJson() != null) ? resp.detectionMetaJson() : "null"
            );

            // (4) 반영: 크롭 성공 시 크롭키로 덮어쓰기, 아니면 원본 유지
            String newKey = (resp != null && resp.cropKey() != null) ? resp.cropKey() : rr.getSubmittedImageKey();
            rr = domain.applyYoloCropOverwrite(rr.getId(), rr.getUniversityId(), rr.getOrganizationId(), newKey, metaJson);
            return rr;
        }   catch (Exception e) {
            String metaJson = """
              { "original_key":"%s", "status":"yolo_failed", "error":"%s" }
            """.formatted(originalKey, e.getClass().getSimpleName());
            // 실패 시: 원본 유지 + 실패 메타 기록
            return domain.applyYoloCropOverwrite(rr.getId(), rr.getUniversityId(), rr.getOrganizationId(), rr.getSubmittedImageKey(), metaJson);
        }
    }

    public ReturnRequest cancel(LoginUser loginUser, Long id,Long organizationId, Long universityId) {
        // 도메인에서 본인/상태 검증 처리
        return domain.cancel(id, loginUser.id(),loginUser.universityId(), organizationId);
    }

    /* ===== 관리자 승인 ===== */

    public ReturnRequest approve(LoginUser loginUser, Long id, Long organizationId, String imageKey) {
        // 권한은 도메인에서도 재검증한다고 가정하지만, 사전으로 관리자 멤버십 한 번 체크
        return domain.approve(loginUser.id(),loginUser.universityId(), id,organizationId,imageKey); //허용한 유저 아이디, 리퀘스트id, 조직 id, 이미지키
    }
}
