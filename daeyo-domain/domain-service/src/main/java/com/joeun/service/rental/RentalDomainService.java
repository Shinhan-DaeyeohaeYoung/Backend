package com.joeun.service.rental;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.entity.IndividualItemStatus;
import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.ItemRepository;
import com.joeun.domain.notification.entity.NotiType;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import com.joeun.domain.rental.repository.RentalRepository;
import com.joeun.domain.reservation.service.ReservationRedisService;
import com.joeun.domain.reservation.vo.ReserveResult;
import com.joeun.global.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RentalDomainService {

    private final ItemRepository itemRepository;
    private final IndividualItemRepository unitRepository;
    private final RentalRepository rentalRepository;
    private final ReservationRedisService reservationRedisService;
    private final ApplicationEventPublisher eventPublisher;

    /** 1) 예약 생성: IndividualItem → RESERVED, Rental(RESERVED) 생성 */
    @Transactional(rollbackFor = Exception.class)
    public Long reserveUnit(Long u, Long o, Long userId, Long itemId, Long unitId, int ttlMinutes) {

        IndividualItem unit = unitRepository.findByIdAndTenant(u, o, unitId)
                .orElseThrow(() -> new NoSuchElementException("unit not found"));

        if (!unit.getItem().getId().equals(itemId))
            throw new IllegalStateException("unit not in this item");

        if (unit.getStatus() != IndividualItemStatus.AVAILABLE)
            throw new IllegalStateException("unit is not AVAILABLE");

        String offerToken = UUID.randomUUID().toString();

        try {

            ReserveResult result = reservationRedisService
                    .doReserve(unitId, offerToken, ttlMinutes * 60);

            if (!result.ok()) {
                throw new IllegalStateException("reserve failed: " + result.reason());
            }

            long expireEpoch = Long.parseLong(result.expireEpoch());
            LocalDateTime expiresUtc = LocalDateTime.ofEpochSecond(expireEpoch, 0, ZoneOffset.UTC);

            int updated = unitRepository.updateStatusIfAvailable(unitId,
                    IndividualItemStatus.RESERVED, IndividualItemStatus.AVAILABLE);

            if (updated != 1) {
                throw new IllegalStateException("unit raced - not AVAILABLE");
            }

            Rental rental = Rental.builder()
                    .universityId(u).organizationId(o)
                    .userId(userId)
                    .item(unit.getItem())
                    .unit(unit)
                    .quantity(1)
                    .status(RentalStatus.RESERVED)
                    .reservedAt(LocalDateTime.now())
                    .reserveExpiresAt(expiresUtc)
                    .offerToken(offerToken)
                    .build();

            eventPublisher.publishEvent(NotificationRequest.builder()
                    .notiType(NotiType.RENTAL_RESERVATION)
                    .userId(userId)
                    .build()
            );

            return rentalRepository.save(rental).getId();
        } catch (Exception e) {
            try {
                reservationRedisService.revertReserve(unitId, offerToken);
                eventPublisher.publishEvent(NotificationRequest.builder()
                        .notiType(NotiType.HOLDING_CANCEL)
                        .userId(userId)
                );
            }
            catch (Exception revertEx) {
                log.error("failed to revert reservation in Redis", revertEx);
            }
            throw new RuntimeException(e);
        }
    }

    /** 2) 내 ‘유효한’ 예약 목록(만료 전) */
    @Transactional(readOnly = true)
    public Page<Rental> listMyActiveReservations(Long u, Long userId, Pageable pageable) {
        return rentalRepository.findAllByUniversityIdAndUserIdAndStatusAndReserveExpiresAtAfter(
                u, userId, RentalStatus.RESERVED, LocalDateTime.now(), pageable);
    }

    /** 3) 예약 확정: RESERVED → RENTED, due 날짜 설정 */
    @Transactional
    public Approved approveReservation(Long u, Long o, Long rentalId, Long actorUserId) {
        Rental r = rentalRepository.lockByIdAndTenant(u, o, rentalId)
                .orElseThrow(() -> new NoSuchElementException("rental not found"));
        if (!r.getUserId().equals(actorUserId)) throw new IllegalStateException("not owner");
        if (r.getStatus() != RentalStatus.RESERVED) throw new IllegalStateException("not RESERVED");
        if (r.isReservationExpired(LocalDateTime.now())) throw new IllegalStateException("reservation expired");

        // 유닛 잠금 + 상태 점검
        var unit = unitRepository.lockByIdAndTenant(u, o, r.getUnit().getId())
                .orElseThrow(() -> new NoSuchElementException("unit not found"));
        if (unit.getStatus() != IndividualItemStatus.RESERVED)
            throw new IllegalStateException("unit not RESERVED");

        // unit RESERVED → RENTED
        unit.changeStatus(IndividualItemStatus.RENTED);

        // due 날짜: 아이템 maxRentalDays (없으면 7일 기본)
        int days = Optional.ofNullable(r.getItem().getMaxRentalDays()).orElse(7);
        var now = LocalDateTime.now();
        r.confirmRental(now, now.plusDays(days));

        return new Approved(r.getId(), r.getStatus(), r.getDueAt());
    }

    @Transactional
    public void onHoldExpired(String holdingId) {

        try {
            Rental rental = rentalRepository.findByOfferTokenForUpdate(holdingId).orElse(null);
            if (rental == null) return;                    // 이미 정리된 케이스

            if (rental.getStatus() != RentalStatus.RESERVED) return;

            if (rental.getReserveExpiresAt().isAfter(Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime())) {
                return;
            }

            rental.expired();
            IndividualItem unit = rental.getUnit();

            if (unit.getStatus() == IndividualItemStatus.RESERVED) {
                unit.changeStatus(IndividualItemStatus.AVAILABLE);
            }

            Long unitId = unit.getId();

            reservationRedisService.cleanupReserve(unitId, holdingId);

        } catch (Exception e) {
            log.error("onHoldExpired error: holdingId={}", holdingId, e);
        }
    }



    /** 승인 결과 스냅샷 */
    public record Approved(Long id, RentalStatus status, LocalDateTime dueAt) {}

    /** 4) 예약 취소: RESERVED → CANCELLED, 유닛 AVAILABLE 복구 */
    @Transactional
    public void cancelReservation(Long u, Long o, Long rentalId, Long actorUserId) {
        Rental r = rentalRepository.lockByIdAndTenant(u, o, rentalId)
                .orElseThrow(() -> new NoSuchElementException("rental not found"));
        if (!r.getUserId().equals(actorUserId)) throw new IllegalStateException("not owner");
        if (r.getStatus() != RentalStatus.RESERVED) return;

        IndividualItem unit = unitRepository.lockByIdAndTenant(u, o, r.getUnit().getId())
                .orElseThrow(() -> new NoSuchElementException("unit not found"));
        if (unit.getStatus() == IndividualItemStatus.RESERVED) {
            unit.changeStatus(IndividualItemStatus.AVAILABLE);
        }
        r.cancelReservation();
    }

    /** 5) 가능 여부 확인 (QR 전 대여 가능 체크) */
    @Transactional(readOnly = true)
    public boolean isRentalPossible(Long u, Long o, Long rentalId, Long actorUserId) {
        return rentalRepository.findByIdAndTenant(u, o, rentalId) // ✅ no lock
                .filter(r -> r.getUserId().equals(actorUserId))
                .filter(r -> r.getStatus() == RentalStatus.RESERVED)
                .filter(r -> !r.isReservationExpired(LocalDateTime.now()))
                .map(r -> r.getUnit() != null
                        && r.getUnit().getStatus() == IndividualItemStatus.RESERVED)
                .orElse(false);
    }
    @Transactional(readOnly = true)
    public List<Rental> findActiveByUnitIds(Long u, Collection<Long> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) return List.of();
        return rentalRepository.findByUnitIdInAndStatusIn(
                unitIds, List.of(RentalStatus.RESERVED, RentalStatus.RENTED)
        );
    }

}