package com.joeun.api.rental.controller;

import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.rental.dto.RentalDtos;
import com.joeun.api.rental.dto.RentalDtos.*;
import com.joeun.api.rental.service.RentalApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class RentalController {

    private final RentalApplicationService app;

    // 1) 대여신청(= 예약) → rental 생성(RESERVED)
    @PostMapping("/rental-requests/reservations")
    public ReserveResponse reserve(@RequestBody ReserveRequest req) {
        Long userId = 1L; // 임시
        return app.reserve(userId, req);
    }

    // 2) 내 대여 신청(예약) 내역 (만료 전, 대여 확정 전)
    @GetMapping("/rental-requests")
    public PageResponse<ReservationSummary> myReservations(
            @PageableDefault(size = 20, sort = "reservedAt") Pageable pageable) {
        Long userId = 1L;
        return PageResponse.from(app.listMyReservations(userId, pageable));
    }

    // 3) 홀딩 물품 대여 가능 확인
    @GetMapping("/rental-requests/{id}/possible")
    public Map<String, Object> possible(@PathVariable Long id) {
        Long userId = 1L;
        boolean ok = app.possible(userId, id);
        return Map.of("id", id, "possible", ok);
    }

    // 4) 대여 확정(RESERVED→RENTED)
    @PostMapping("/rental-requests/{id}/approve")
    public RentalDtos.ApproveResponse approve(@PathVariable Long id) {
        Long userId = 1L;
        return app.approve(userId, id);
    }

    // 5) 대여 신청 취소(RESERVED→CANCELLED)
    @PatchMapping("/rental-requests/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id) {
        Long userId = 1L;
        app.cancel(userId, id);
        return Map.of("id", id, "cancelled", true);
    }
}

