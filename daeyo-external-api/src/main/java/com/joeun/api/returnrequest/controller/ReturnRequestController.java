package com.joeun.api.returnrequest.controller;

import com.joeun.api.returnrequest.dto.*;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.domain.returnrequest.service.ReturnRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Return Requests", description = "반납 신청/승인/취소 및 조회")
@RestController
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService service;

    /** 1) (관리자) 반납 승인(신청) 목록 */
    @Operation(
            summary = "반납 신청 목록 (관리자)",
            description = "테넌트 범위(universityId, organizationId) 내 반납 신청 목록을 페이지로 조회합니다. status 미지정 시 전체."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReturnRequestResponse.class)
            ))
    })
    @GetMapping("/admin/return-requests")
    public ResponseEntity<Page<ReturnRequestResponse>> listForAdmin(
            @Parameter(description = "대학 ID", example = "1") @RequestParam Long universityId,
            @Parameter(description = "조직 ID", example = "2") @RequestParam Long organizationId,
            @Parameter(description = "상태 필터", example = "REQUESTED") @RequestParam(required = false) ReturnRequestStatus status,
            @ParameterObject Pageable pageable
    ) {
        var page = service.listForAdmin(universityId, organizationId, status, pageable)
                .map(ReturnRequestResponse::from);
        return ResponseEntity.ok(page);
    }

    /** 2) (관리자) 반납 승인 상세보기 */
    @Operation(
            summary = "반납 신청 상세 (관리자)",
            description = "테넌트 범위 내 특정 반납 신청 상세를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReturnRequestResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @GetMapping("/admin/return-requests/{id}")
    public ResponseEntity<ReturnRequestResponse> detailForAdmin(
            TenantProvider tenant,
            @PathVariable Long id
    ) {
        var rr = service.detailForAdmin(tenant.universityId(), tenant.organizationId(), id);
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }


    /** 3) (유저) 반납 신청  */
    @Operation(
            summary = "반납 신청 생성 (유저)",
            description = "유저가 대여건에 대해 반납 신청을 생성합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReturnRequestResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "유효성 오류"),
            @ApiResponse(responseCode = "404", description = "대여건 없음")
    })
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
    @Operation(
            summary = "반납 승인 (관리자)",
            description = "반납 신청을 승인하고, 개별상품 상태를 AVAILABLE로 전환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ReturnRequestResponse.class)
            )),
            @ApiResponse(responseCode = "404", description = "대상 없음"),
            @ApiResponse(responseCode = "409", description = "잘못된 상태 전이")
    })
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
@Operation(
        summary = "반납 신청 취소 (유저)",
        description = "유저가 본인의 반납 신청을 취소합니다."
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "취소 성공", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReturnRequestResponse.class)
        )),
        @ApiResponse(responseCode = "404", description = "대상 없음"),
        @ApiResponse(responseCode = "409", description = "잘못된 상태 전이")
})
    @PatchMapping(value = "/return-requests/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReturnRequestResponse> cancel(
            @PathVariable Long id,
            @RequestBody CancelReturnRequest req
    ) {
        var rr = service.cancel(id, req.userId());
        return ResponseEntity.ok(ReturnRequestResponse.from(rr));
    }
}
