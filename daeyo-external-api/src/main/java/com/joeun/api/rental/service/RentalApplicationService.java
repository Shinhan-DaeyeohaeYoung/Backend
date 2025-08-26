package com.joeun.api.rental.service;
import com.joeun.domain.rental.entity.RentalStatus;

import com.joeun.api.rental.dto.RentalDtos;
import com.joeun.api.rental.dto.RentalDtos.*;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import com.joeun.service.rental.RentalDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalApplicationService {

    private final TenantProvider tenant;
    private final RentalDomainService domain;

    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ReserveResponse reserve(Long userId, ReserveRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        int ttl = (req.ttlMinutes() == null || req.ttlMinutes() <= 0) ? 30 : req.ttlMinutes();
        Long rentalId = domain.reserveUnit(u, o, userId, req.itemId(), req.unitId(), ttl);
        // 간단 응답(필요시 재조회로 시간 채우기)
        return new ReserveResponse(rentalId, req.itemId(), req.unitId(), null, null, "RESERVED");
    }

    public Page<Rental> myReserved(Long userId, Pageable pageable) {
        Long u = tenant.universityId();
        return domain.listMyActiveReservations(u, userId, pageable);
    }
    public Page<RentalDtos.ReservationSummary> listMyReservations(Long userId, Pageable pageable) {
        Long u = tenant.universityId();
        return domain.listMyActiveReservations(u, userId, pageable)
                .map(r -> new RentalDtos.ReservationSummary(
                        r.getId(),
                        r.getItem().getId(),
                        r.getUnit().getId(),
                        r.getStatus().name(),
                        r.getReservedAt() == null ? null : r.getReservedAt().format(F),
                        r.getReserveExpiresAt() == null ? null : r.getReserveExpiresAt().format(F)
                ));
    }

    public RentalDtos.ApproveResponse approve(Long userId, Long rentalId) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var approved = domain.approveReservation(u, o, rentalId, userId);
        return new RentalDtos.ApproveResponse(
                approved.id(),
                approved.status().name(),
                approved.dueAt() == null ? null : approved.dueAt().format(F)
        );
    }

    public void cancel(Long userId, Long rentalId) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        domain.cancelReservation(u, o, rentalId, userId);
    }

    public boolean possible(Long userId, Long rentalId) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        return domain.isRentalPossible(u, o, rentalId, userId);
    }
    public Page<RentalDtos.RentalHistoryItem> listMyRentalHistory(
            Long userId,
            String statusCsv,                 // "RENTED,RETURNED" 형태. null/빈값이면 전체
            String fromIso,                   // ISO-8601 문자열. null 허용
            String toIso,                     // ISO-8601 문자열. null 허용
            boolean includeExpiredReservations,
            Pageable pageable
    ) {
        Long u = tenant.universityId();

        Set<RentalStatus> statuses = parseStatuses(statusCsv);
        LocalDateTime from = parseDateTime(fromIso);
        LocalDateTime to = parseDateTime(toIso);

        Page<Rental> page = domain.listMyHistory(
                u, userId, statuses, from, to, includeExpiredReservations, pageable
        );

        final LocalDateTime now = LocalDateTime.now();

        return page.map(r -> {
            boolean expired = r.getStatus() == RentalStatus.RESERVED
                    && r.getReserveExpiresAt() != null
                    && r.getReserveExpiresAt().isBefore(now);

            return new RentalDtos.RentalHistoryItem(
                    r.getId(),
                    r.getStatus().name(),
                    r.getItem() != null ? r.getItem().getId() : null,
                    r.getUnit() != null ? r.getUnit().getId() : null,
                    r.getReservedAt() == null ? null : r.getReservedAt().format(F),
                    r.getReserveExpiresAt() == null ? null : r.getReserveExpiresAt().format(F),
                    r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                    r.getDueAt() == null ? null : r.getDueAt().format(F),
                    r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                    expired
            );
        });
    }

    // ---------- helpers ----------
    private Set<RentalStatus> parseStatuses(String statusCsv) {
        if (statusCsv == null || statusCsv.isBlank()) return Collections.emptySet();
        return Arrays.stream(statusCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(RentalStatus::valueOf) // 잘못된 값이면 IllegalArgumentException → @ControllerAdvice에서 400 처리 권장
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(RentalStatus.class)));
    }

    private LocalDateTime parseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        return LocalDateTime.parse(iso, F);
    }

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "id",
            "status",
            "reservedAt",
            "reserveExpiresAt",
            "rentedAt",
            "dueAt",
            "returnedAt",
            "organizationId",
            "universityId",
            "userId"
    );

    private Pageable sanitizeSort(Pageable pageable, String fallbackProperty) {
        if (pageable == null) return PageRequest.of(0, 20, Sort.by(Sort.Order.desc(fallbackProperty)));

        Sort safe = Sort.unsorted();
        for (Sort.Order o : pageable.getSort()) {
            String p = o.getProperty();
            if (ALLOWED_SORTS.contains(p)) {
                safe = safe.and(Sort.by(new Sort.Order(o.getDirection(), p)));
            }
        }
        if (safe.isUnsorted()) {
            safe = Sort.by(Sort.Order.desc(fallbackProperty)); // 기본 정렬
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safe);
    }

    // --- 사용처 1: 내 대여중 전체 (/rentals/myitems)
    public Page<RentalDtos.CurrentRentalItem> listMyCurrentRentals(Long userId, Pageable pageable) {
        Long u = tenant.universityId();
        Pageable safe = sanitizeSort(pageable, "rentedAt"); // ✅ string 같은 값 걸러짐

        Page<Rental> page = domain.listMyCurrentRentals(u, userId, safe);
        return page.map(r -> new RentalDtos.CurrentRentalItem(
                r.getId(),
                r.getUniversityId(),
                r.getOrganizationId(),
                r.getUserId(),
                r.getItem() != null ? r.getItem().getId() : null,
                r.getUnit() != null ? r.getUnit().getId() : null,
                r.getQuantity(),
                r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                r.getDueAt() == null ? null : r.getDueAt().format(F),
                r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                r.getStatus().name(),
                /* depositId: 엔티티 구조에 맞게 */
                null
        ));
    }

    // --- 사용처 2: 조직별 내 대여중 (/rentals/myitems/organizations/{organizationId})
    public Page<RentalDtos.CurrentRentalItem> listMyCurrentRentalsByOrganization(
            Long userId, Long organizationId, Pageable pageable
    ) {
        Long u = tenant.universityId();
        Pageable safe = sanitizeSort(pageable, "rentedAt"); // ✅ 동일하게 보호

        Page<Rental> page = domain.listMyCurrentRentalsByOrganization(u, organizationId, userId, safe);
        return page.map(r -> new RentalDtos.CurrentRentalItem(
                r.getId(),
                r.getUniversityId(),
                r.getOrganizationId(),
                r.getUserId(),
                r.getItem() != null ? r.getItem().getId() : null,
                r.getUnit() != null ? r.getUnit().getId() : null,
                r.getQuantity(),
                r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                r.getDueAt() == null ? null : r.getDueAt().format(F),
                r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                r.getStatus().name(),
                /* depositId: 엔티티 구조에 맞게 */
                null
        ));
    }


    public Page<RentalDtos.ReservationSummary> listMyReservationsByOrganization(
            Long userId, Long organizationId, Pageable pageable
    ) {
        Long u = tenant.universityId();
        return domain.listMyActiveReservationsByOrganization(u, organizationId, userId, pageable)
                .map(r -> new RentalDtos.ReservationSummary(
                        r.getId(),
                        r.getItem().getId(),
                        r.getUnit().getId(),
                        r.getStatus().name(),
                        r.getReservedAt() == null ? null : r.getReservedAt().format(F),
                        r.getReserveExpiresAt() == null ? null : r.getReserveExpiresAt().format(F)
                ));
    }

}
