package com.joeun.api.rental.service;

import com.joeun.api.rental.dto.RentalDtos;
import com.joeun.api.rental.dto.RentalDtos.*;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.service.rental.RentalDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

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
}
