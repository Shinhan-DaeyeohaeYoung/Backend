package com.joeun.api.rental.controller;

import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.rental.dto.RentalDtos;
import com.joeun.api.rental.dto.RentalDtos.*;
import com.joeun.api.rental.service.RentalApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Rentals", description = "대여(예약/확정/취소) API")
public class RentalController {

    private final RentalApplicationService app;

    // 1) 대여신청(= 예약) → rental 생성(RESERVED)
    @Operation(
            summary = "대여 예약 생성(유닛 홀드)",
            description = "개별자산을 RESERVED 상태로 홀드하고 rental 레코드를 RESERVED로 생성합니다. "
                    + "예약은 TTL(예: 30분) 내에만 유효하며, 미확정 시 만료됩니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예약 생성 성공",
                    content = @Content(
                            schema = @Schema(implementation = ReserveResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "id": 12,
                      "status": "RESERVED",
                      "itemId": 2,
                      "unitId": 4,
                      "reservedAt": "2025-08-24T22:13:42.275928",
                      "reserveExpiresAt": "2025-08-24T22:43:42.275928"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "요청 값 오류"),
            @ApiResponse(responseCode = "409", description = "이미 대여중/예약불가(상태 충돌)"),
            @ApiResponse(responseCode = "404", description = "아이템/유닛 없음")
    })
    @PostMapping("/rental-requests/reservations")
    public ReserveResponse reserve(@RequestBody ReserveRequest req) {
        Long userId = 1L; // 임시
        return app.reserve(userId, req);
    }

    // 2) 내 대여 신청(예약) 내역 (만료 전, 대여 확정 전)
    @Operation(
            summary = "내 예약 목록(유효한 것만)",
            description = "현재 시간 기준 만료되지 않은 RESERVED 상태의 예약 목록을 페이징으로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "content": [
                        {
                          "id": 3,
                          "status": "RESERVED",
                          "itemId": 2,
                          "unitId": 4,
                          "reservedAt": "2025-08-24T22:13:42.275928",
                          "reserveExpiresAt": "2025-08-24T22:43:42.275928"
                        }
                      ],
                      "page": 0,
                      "size": 20,
                      "totalElements": 1
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/rental-requests")
    public PageResponse<ReservationSummary> myReservations(
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "reservedAt") Pageable pageable) {
        Long userId = 1L;
        return PageResponse.from(app.listMyReservations(userId, pageable));
    }

    // 3) 홀딩 물품 대여 가능 확인
    @Operation(
            summary = "홀딩 물품 대여 가능 여부 확인",
            description = "예약이 본인 소유이며 만료 전이고 유닛 상태가 RESERVED인지 검증합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검증 결과",
                    content = @Content(
                            schema = @Schema(example = "{\"id\": 3, \"possible\": true}")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "예약 없음(권한 또는 ID 오류)")
    })
    @GetMapping("/rental-requests/{id}/possible")
    public Map<String, Object> possible(@PathVariable Long id) {
        Long userId = 1L;
        boolean ok = app.possible(userId, id);
        return Map.of("id", id, "possible", ok);
    }

    // 4) 대여 확정(RESERVED→RENTED)
    @Operation(
            summary = "대여 확정(RESERVED → RENTED)",
            description = "QR 확인 등 현장 절차 후 대여를 확정합니다. 유닛은 RENTED로, rental은 RENTED로 전이되며 dueAt을 설정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "확정 성공",
                    content = @Content(
                            schema = @Schema(implementation = RentalDtos.ApproveResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "id": 12,
                      "status": "RENTED",
                      "dueAt": "2025-08-31T12:00:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "만료/상태 전이 불가"),
            @ApiResponse(responseCode = "403", description = "본인 소유 아님"),
            @ApiResponse(responseCode = "404", description = "예약 없음")
    })
    @PostMapping("/rental-requests/{id}/approve")
    public RentalDtos.ApproveResponse approve(@PathVariable Long id) {
        Long userId = 1L;
        return app.approve(userId, id);
    }

    // 5) 대여 신청 취소(RESERVED→CANCELLED)
    @Operation(
            summary = "대여 예약 취소(RESERVED → CANCELLED)",
            description = "본인 예약을 취소하고 유닛 상태를 AVAILABLE로 복구합니다(가능한 경우)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "취소 성공",
                    content = @Content(schema = @Schema(example = "{\"id\": 12, \"cancelled\": true}"))
            ),
            @ApiResponse(responseCode = "403", description = "본인 소유 아님"),
            @ApiResponse(responseCode = "404", description = "예약 없음")
    })
    @PatchMapping("/rental-requests/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id) {
        Long userId = 1L;
        app.cancel(userId, id);
        return Map.of("id", id, "cancelled", true);
    }


    // RentalController.java (추가)
    // 6) 내 대여 이력 전체(과거 포함) 조회
    @Operation(
            summary = "내 대여 이력 조회(과거 포함)",
            description = """
                사용자의 모든 대여 이력을 페이지로 반환합니다.
                - 상태 필터링: RESERVED / RENTED / CANCELLED / RETURNED 등 상태로 필터링 가능
                - 기간 필터링: from ~ to (ISO-8601, 예: 2025-08-01T00:00:00)
                - includeExpiredReservations: true이면 만료된 RESERVED 예약도 포함
                기본 정렬은 최신순(updatedAt 또는 상태 전이 일시)입니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "content": [
                        {
                          "id": 42,
                          "status": "RENTED",
                          "itemId": 2,
                          "unitId": 4,
                          "reservedAt": "2025-08-20T11:30:12.100",
                          "reserveExpiresAt": "2025-08-20T12:00:12.100",
                          "rentedAt": "2025-08-20T11:45:01.000",
                          "dueAt": "2025-08-27T12:00:00",
                          "returnedAt": null,
                          "expired": false
                        }
                      ],
                      "page": 0,
                      "size": 20,
                      "totalElements": 2
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/rentals")
    public PageResponse<RentalHistoryItem> myRentalHistory(
            @Parameter(description = "상태 필터(콤마 구분). 예: RENTED,RETURNED. 미지정 시 모두")
            @RequestParam(required = false) String status,

            @Parameter(description = "조회 시작 시각(ISO-8601). 예: 2025-08-01T00:00:00")
            @RequestParam(required = false) String from,

            @Parameter(description = "조회 종료 시각(ISO-8601). 예: 2025-08-31T23:59:59")
            @RequestParam(required = false) String to,

            @Parameter(description = "만료된 RESERVED 예약 포함 여부(기본: false)")
            @RequestParam(required = false, defaultValue = "false") boolean includeExpiredReservations,

            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "rentedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = 1L;
        return PageResponse.from(
                app.listMyRentalHistory(userId, status, from, to, includeExpiredReservations, pageable)
        );
    }



    @Operation(
            summary = "내 현재 대여중 목록",
            description = "상태가 REN​TED인 대여만 페이지로 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(
                                    value = """
{
  "content": [
    {
      "id": 9001,
      "universityId": 1,
      "userId": 321,
      "itemId": 11,
      "organizationId": 201,
      "individualItemId": 501,
      "quantity": 1,
      "rentedAt": "2025-08-20T12:00:00",
      "dueAt": "2025-08-27T12:00:00",
      "returnedAt": null,
      "status": "RENTED",
      "depositId": 700
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1
}
"""
                            )
                    )
            )
    })
    @GetMapping("/rentals/myitems")
    public PageResponse<RentalDtos.CurrentRentalItem> myCurrentRentals(
            @PageableDefault(size = 20, sort = "rentedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = 1L;
        return PageResponse.from(app.listMyCurrentRentals(userId, pageable));
    }

    @Operation(
            summary = "내 홀딩 예약 중 특정 조직의 것만",
            description = "현재 사용자(userId)의 RESERVED 상태이면서 만료 전이고, 전달한 organizationId에 속한 예약만 페이지로 반환합니다."
    )
    @GetMapping("/rental-requests/{organizationId}/holding")
    public PageResponse<RentalDtos.ReservationSummary> myOrgReservations(
            @PathVariable Long organizationId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "reservedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = 1L; // TODO: 인증 연동 시 교체
        return PageResponse.from(app.listMyReservationsByOrganization(userId, organizationId, pageable));
    }
    @Operation(
            summary = "내 대여중(REN​TED) 목록 - 특정 조직만",
            description = "현재 사용자(userId)가 RENTED 상태이며 아직 반납되지 않은 대여 중, 전달한 organizationId에 속한 것만 페이지로 반환합니다."
    )
    @GetMapping("/rentals/organizations/{organizationId}")
    public PageResponse<RentalDtos.CurrentRentalItem> myCurrentRentalsByOrg(
            @PathVariable Long organizationId,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "rentedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = 1L; // TODO: 인증 연동 시 교체
        return PageResponse.from(app.listMyCurrentRentalsByOrganization(userId, organizationId, pageable));
    }


}

