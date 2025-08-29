package com.joeun.api.returnrequest.service;

import com.joeun.api.deposit.service.DepositService;
import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.api.returnrequest.dto.*;
import com.joeun.api.user.service.UserService;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.global.config.LoginUser;
import com.joeun.infra.aws.s3.service.S3ImageInfraService;
import com.joeun.service.rental.RentalDomainService;
import com.joeun.service.returnrequest.ReturnRequestQueryService;
import com.joeun.service.returnrequest.ReturnRequestService;
import com.joeun.service.user.UserDomainService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReturnRequestApplicationService {

    private final ReturnRequestService domain;            // 도메인 서비스(기존)
    private final OrganizationService organizationService; // 멤버십/관리자 확인
    private final RentalDomainService rentalDomainService; // rentalId → (u,o,owner) 해석
    private final UserDomainService userDomainService;
    private final DepositService depositService;
    private final S3ImageInfraService s3ImageInfraService;
    private final ReturnRequestQueryService rrQueryService;

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

    public Page<ReturnRequestResponse> listForAdminWithUrls(
            LoginUser loginUser, Long organizationId, ReturnRequestStatus status, Pageable pageable) {

        var admin = pickAdminOrg(loginUser, organizationId);
        Page<ReturnRequest> page = domain.listForAdmin(
                admin.getUniversityId(), admin.getOrganizationId(), status, pageable);

        return page.map(rr -> {
            String key = rr.getSubmittedImageKey();
            String url = (key == null || key.isBlank()) ? null
                    : s3ImageInfraService.getDownloadPresignedUrl(key);
            return ReturnRequestResponse.from(rr, url);
        });
    }
    public ReturnRequestResponse detailForAdminWithUrls(
            LoginUser loginUser, Long id, Long organizationId) {

        var admin = pickAdminOrg(loginUser, organizationId);
        var rr = domain.detailForAdmin(admin.getUniversityId(), admin.getOrganizationId(), id);

        var keys = rrQueryService.getBeforeAfterKeys(id);
        String afterKey = keys.afterKey();
        String afterUrl = (afterKey == null || afterKey.isBlank()) ? null
                : s3ImageInfraService.getDownloadPresignedUrl(afterKey);

         String beforeKey = keys.beforeKey();
         String beforeUrl = (beforeKey == null || beforeKey.isBlank()) ? null
                : s3ImageInfraService.getDownloadPresignedUrl(beforeKey);

//        return ReturnRequestResponse.from(rr, afterUrl);
         return ReturnRequestResponse.from(rr, afterUrl, beforeKey, beforeUrl);
    }
    /* ===== 관리자 목록/상세 ===== */
//    public Page<ReturnRequestResponse> listForAdmin(
//            LoginUser loginUser, Long organizationId, ReturnRequestStatus status, Pageable pageable) {
//        var page = domain.listForAdmin(loginUser.universityId(), organizationId, status, pageable);
//
//        return page.map(rr -> {
//            String key = rr.getSubmittedImageKey();
//            String url = (key == null || key.isBlank()) ? null
//                    : s3ImageInfraService.getDownloadPresignedUrl(key); // 필요시 TTL 오버로드
//            return ReturnRequestResponse.from(rr, url);
//        });
//    }


//    public Page<ReturnRequest> listForAdmin(LoginUser loginUser, Long organizationId, ReturnRequestStatus status, Pageable pageable) {
//        var admin = pickAdminOrg(loginUser, organizationId);
//        return domain.listForAdmin(admin.getUniversityId(), admin.getOrganizationId(), status, pageable);
//    }

    public ReturnRequestResponse detailForAdmin(LoginUser loginUser, Long id, Long organizationId) {
        var rr = domain.detailForAdmin(loginUser.universityId(), organizationId, id);

        // before/after 키
        var keys = rrQueryService.getBeforeAfterKeys(id);
        String beforeKey = keys.beforeKey();
        String afterKey  = keys.afterKey(); // rr.getSubmittedImageKey()와 동일

        String beforeUrl = (beforeKey == null || beforeKey.isBlank()) ? null
                : s3ImageInfraService.getDownloadPresignedUrl(beforeKey);
        String afterUrl  = (afterKey == null || afterKey.isBlank()) ? null
                : s3ImageInfraService.getDownloadPresignedUrl(afterKey);

        return ReturnRequestResponse.from(rr, afterUrl, beforeKey, beforeUrl);
    }

    /* ===== 유저 생성/취소 ===== */

    public ReturnRequest create(LoginUser loginUser, ReturnRequestCreateRequest req) {
        // rentalId 기준으로 (u,o,owner) 해석 + 소유자 검증
        Rental r = rentalDomainService.getById(req.rentalId()); // ← 없으면 간단히 추가
        if (!Objects.equals(r.getUserId(), loginUser.id())) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the owner of this rental");
        }
        return domain.create(
                r.getUniversityId(), r.getOrganizationId(), loginUser.id(),
                req.rentalId(), req.imageKey(), req.imageMime(), req.imageHash(), req.imageTakenAt()
        );
    }

    public ReturnRequest cancel(LoginUser loginUser, Long id,Long organizationId, Long universityId) {
        // 도메인에서 본인/상태 검증 처리
        return domain.cancel(id, loginUser.id(),loginUser.universityId(), organizationId);
    }

    /* ===== 관리자 승인 ===== */

    public ReturnRequest approve(LoginUser loginUser, Long id, Long organizationId, String imageKey) {
        // 권한은 도메인에서도 재검증한다고 가정하지만, 사전으로 관리자 멤버십 한 번 체크
/*        ReturnRequest approve = domain.approve(loginUser.id(), loginUser.universityId(), id,
            organizationId, imageKey);//허용한 유저 아이디, 리퀘스트id, 조직 id, 이미지키

        // 점수 올려
        Long userId = approve.getUserId();
        userDomainService.addOnRefund(userId);

        return approve;*/
        Long u = loginUser.universityId();

        // 1) 환불 대상/금액 사전 조회 (도메인, readOnly)
        Long userId = domain.getReturnRequestUserId(u, organizationId, id);
        BigDecimal amount = domain.getRefundAmountOrThrow(u, organizationId, id);

        // 2) 조직 → 사용자 환불 이체 (외부 API 호출은 App 레이어에서만)
        String memo = "보증금 환불 (returnRequestId=" + id + ")";
        depositService.transferOrganizationToUser(organizationId, userId, amount, memo);

        // 3) 도메인 승인 (유닛 AVAILABLE, 상태 전이 등)
        ReturnRequest rr = domain.approve(loginUser.id(), loginUser.universityId(), id, organizationId, imageKey);

        // 4) 사용자 포인트/점수 반영 (요청된 로직 유지)
        userDomainService.addOnRefund(rr.getUserId());

        return rr;

    }
}
