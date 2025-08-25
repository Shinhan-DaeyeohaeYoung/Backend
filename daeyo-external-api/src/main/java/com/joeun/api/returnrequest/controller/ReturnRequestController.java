package com.joeun.api.returnrequest.controller;

import com.joeun.api.returnrequest.dto.*;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.domain.returnrequest.service.ReturnRequestService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService service;

    /** 1) (관리자) 반납 승인(신청) 목록 */
    @GetMapping("/admin/return-requests")
    public ResponseEntity<Page<ReturnRequestResponse>> listForAdmin(
            @RequestParam Long universityId,
            @RequestParam Long organizationId,
            @RequestParam(required = false) ReturnRequestStatus status,
            @ParameterObject Pageable pageable
    ) {
        var page = service.listForAdmin(universityId, organizationId, status, pageable)
                .map(ReturnRequestResponse::from);
        return ResponseEntity.ok(page);
    }

    /** 2) (관리자) 반납 승인 상세보기 */
    @GetMapping("/admin/return-requests/{id}")
    public ResponseEntity<ReturnRequestResponse> detailForAdmin(
            TenantProvider tenant,
            @PathVariable Long id
    ) {
        var rr = service.detailForAdmin(tenant.universityId(), tenant.organizationId(), id);
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }


    /** 3) (유저) 반납 신청  */
    @PostMapping(value = "/return-requests", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> create(
            @RequestBody ReturnRequestCreateRequest req
    ) {
        var rr = service.create(
                req.universityId(), req.organizationId(), req.userId(),
                req.rentalId(), req.imageKey(), req.imageMime(), req.imageHash(), req.imageTakenAt()
        );
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }

    /** 4) (관리자) 물품 반납 승인(+포인트) */
    @PostMapping(value = "/admin/return-requests/{id}/approve", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> approve(
            @PathVariable Long id,
            @RequestBody ApproveReturnRequestRequest req
    ) {
        var rr = service.approve(req.approverUserId(), id);
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }

//    /** 5) 파손률 GPT — GET (쿼리파라미터) */
//    @GetMapping("/return-requests/{id}/damage/suggestions")
//    public ResponseEntity<DamageSuggestionResponse> damageSuggestions(
//            @PathVariable Long id,
//            @RequestParam Long universityId,
//            @RequestParam(required = false) Long userId // 본인 조회 허용 시 사용
//    ) {
//        // ⚠️ 주의: 아래 서비스 메서드가 주석 처리돼 있으면 복구하세요.
//        var r = service.getDamageSuggestions(universityId, id, userId);
//        var body = new DamageSuggestionResponse(r.damageRate(), r.summary(),
//                r.notes() == null ? java.util.List.of() : java.util.List.of(r.notes()),
//                r.suggestedCompensation());
//        return ResponseEntity.ok(body);
//    }

//    /** 6) (유저) 반납 신청 취소 — JSON */
    @PatchMapping(value = "/return-requests/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> cancel(
            @PathVariable Long id,
            @RequestBody CancelReturnRequest req
    ) {
        var rr = service.cancel(id, req.userId());
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }
}
