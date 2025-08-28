package com.joeun.api.returnrequest.controller;

import com.joeun.api.returnrequest.dto.*;
import com.joeun.api.returnrequest.service.ReturnRequestApplicationService;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.global.config.LoginUser;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Return Requests", description = "반납 신청/승인/취소 및 조회")
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestApplicationService app; // ✅ 애플리케이션 서비스 사용

    /** 1) (관리자) 반납 신청 목록 */
    @Operation(summary = "반납 신청 목록 (관리자)",
            description = "관리자 멤버십의 universityId/organizationId 범위에서 반납 신청 목록 조회")
    @GetMapping("/admin/return-requests")
    public ResponseEntity<Page<ReturnRequestResponse>> listForAdmin(
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(description = "조직 ID(선택). 관리자 멤버십이 여러개면 필수") @RequestParam(required = false) Long organizationId,
            @Parameter(description = "상태 필터", example = "REQUESTED") @RequestParam(required = false) ReturnRequestStatus status,
            @ParameterObject Pageable pageable
    ) {
        var page = app.listForAdmin(loginUser, organizationId, status, pageable)
                .map(ReturnRequestResponse::from);
        return ResponseEntity.ok(page);
    }

    /** 2) (관리자) 반납 신청 상세보기 */
    @Operation(summary = "반납 신청 상세 (관리자)",
            description = "관리자 멤버십의 universityId/organizationId 범위에서 특정 반납 신청 상세 조회")
    @GetMapping("/admin/return-requests/{id}")
    public ResponseEntity<ReturnRequestResponse> detailForAdmin(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestParam(required = false) Long organizationId
    ) {
        var rr = app.detailForAdmin(loginUser, id, organizationId);
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }

    /** 3) (유저) 반납 신청 */
    @Operation(summary = "반납 신청 생성 (유저)", description = "로그인 유저가 본인의 대여건에 대해 반납 신청 생성")
    @PostMapping(value = "/return-requests", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> create(
            @AuthenticationPrincipal LoginUser loginUser,
            @RequestBody ReturnRequestCreateRequest req
    ) {
        var rr = app.create(loginUser, req);
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }

    /** 4) (관리자) 반납 승인 */
    @Operation(summary = "반납 승인 (관리자)", description = "반납 신청 승인 및 유닛 상태 AVAILABLE 전환")
    @PostMapping(value = "/admin/return-requests/{id}/approve", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> approve(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody ApproveReturnRequestRequest req
    ) {
        var rr = app.approve(loginUser, id, req.organizationId(), req.imageKey());
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }

    /** 6) (유저) 반납 신청 취소 */
    @Operation(summary = "반납 신청 취소 (유저)", description = "유저가 본인의 반납 신청을 취소")
    @PatchMapping(value = "/return-requests/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> cancel(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long id,
            @RequestBody CancelReturnRequest req
    ) {
        var rr = app.cancel(loginUser, id, req.organizationId(), req.organizationId());
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }
}
