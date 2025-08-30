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
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final ZoneId Z = ZoneOffset.UTC;

    @Transactional(readOnly = true)
    public Rental getById(Long id) {
        return rentalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("rental not found: id=" + id));
    }
    /** 1) 예약 생성: IndividualItem → RESERVED, Rental(RESERVED) 생성 */
//    @Transactional(rollbackFor = Exception.class)
//    public Long reserveUnit(Long u, Long o, Long userId, Long itemId, Long unitId, int ttlMinutes) {
//
//        IndividualItem unit = unitRepository.findByIdAndTenant(u, o, unitId)
//                .orElseThrow(() -> new NoSuchElementException("unit not found"));
//
//        if (!unit.getItem().getId().equals(itemId))
//            throw new IllegalStateException("unit not in this item");
//
//        if ((unit.getStatus() != IndividualItemStatus.AVAILABLE)
//        && (unit.getStatus() != IndividualItemStatus.WAIT_RESERVED))
//            throw new IllegalStateException("unit is not AVAILABLE");
//
//        String offerToken = UUID.randomUUID().toString();
//
//        try {
//
//            ReserveResult result = reservationRedisService
//                    .doReserve(unitId, offerToken, ttlMinutes * 60);
//
//            if (!result.ok()) {
//                throw new IllegalStateException("reserve failed: " + result.reason());
//            }
//
//            long expireEpoch = Long.parseLong(result.expireEpoch());
//            LocalDateTime expiresUtc = LocalDateTime.ofEpochSecond(expireEpoch, 0, ZoneOffset.UTC);
//
//            int updated = unitRepository.updateStatusIfAvailable(unitId,
//                    IndividualItemStatus.RESERVED, IndividualItemStatus.AVAILABLE);
//
//            if (updated != 1) {
//                throw new IllegalStateException("unit raced - not AVAILABLE");
//            }
//
//            Rental rental = Rental.builder()
//                    .universityId(u).organizationId(o)
//                    .userId(userId)
//                    .item(unit.getItem())
//                    .unit(unit)
//                    .quantity(1)
//                    .status(RentalStatus.RESERVED)
//                    .reservedAt(LocalDateTime.now())
//                    .reserveExpiresAt(expiresUtc)
//                    .offerToken(offerToken)
//                    .build();
//
//            eventPublisher.publishEvent(NotificationRequest.builder()
//                    .notiType(NotiType.RENTAL_RESERVATION)
//                    .userId(userId)
//                    .build()
//            );
//
//            return rentalRepository.save(rental).getId();
//        } catch (Exception e) {
//            try {
//                reservationRedisService.revertReserve(unitId, offerToken);
//                eventPublisher.publishEvent(NotificationRequest.builder()
//                        .notiType(NotiType.HOLDING_CANCEL)
//                        .userId(userId)
//                );
//            }
//            catch (Exception revertEx) {
//                log.error("failed to revert reservation in Redis", revertEx);
//            }
//            throw new RuntimeException(e);
//        }
//    }


    @Transactional(rollbackFor = Exception.class)
    public Rental reserveUnit(Long u, Long o, Long userId, Long itemId, Long unitId, int ttlMinutes) {

        IndividualItem unit = unitRepository.findByIdAndTenant(u, o, unitId)
                .orElseThrow(() -> new NoSuchElementException("unit not found"));

        if (!unit.getItem().getId().equals(itemId))
            throw new IllegalStateException("unit not in this item");

        // (선택) 사전 상태 체크: 실제 판정은 아래 조건부 UPDATE가 함
        if ((unit.getStatus() != IndividualItemStatus.AVAILABLE)
                && (unit.getStatus() != IndividualItemStatus.WAIT_RESERVED))
            throw new IllegalStateException("unit is not AVAILABLE");

        String offerToken = UUID.randomUUID().toString();

        try {
            // 1) 분산락(NX)
            ReserveResult result = reservationRedisService
                    .doReserve(unitId, offerToken, ttlMinutes * 60);
            if (!result.ok()) {
                throw new IllegalStateException("reserve failed: " + result.reason());
            }

            long expireEpoch = Long.parseLong(result.expireEpoch());
            LocalDateTime expiresUtc = LocalDateTime.ofInstant(Instant.ofEpochSecond(expireEpoch), Z);

            // 2) 상태 전환 시도 (두 단계)
            // 2-1) AVAILABLE → RESERVED (+ item.available_quantity -1)
            int updated = unitRepository.reserveFromAvailableAndDecrement(unitId);

            if (updated == 2) {
                // AVAILABLE 경로 성공(유닛 1 + 아이템 1 = 2행 갱신)
            } else if (updated == 0) {
                // AVAILABLE 경로 실패 → WAIT_RESERVED 경로 시도(수량 변화 없음)
                updated = unitRepository.reserveFromWaitReservedNoChange(unitId);
                if (updated != 1) {
                    throw new IllegalStateException("unit raced - not AVAILABLE/WAIT_RESERVED");
                }
            } else {
                // 드라이버/옵션(useAffectedRows 등)에 따라 1이 올 수도 있으니 성공으로 처리
            }

            unitRepository.flush(); // DB 반영 보장(선택)

            // 3) 예약 레코드 생성
            Rental rental = Rental.builder()
                    .universityId(u).organizationId(o)
                    .userId(userId)
                    .item(unit.getItem())
                    .unit(unit)
                    .quantity(1)
                    .status(RentalStatus.RESERVED)
                    .reservedAt(expiresUtc)
                    .reserveExpiresAt(expiresUtc.plusMinutes(ttlMinutes))
                    .offerToken(offerToken)
                    .build();

            Long rentalId = rentalRepository.save(rental).getId();

            // 4) 성공 알림 (원하면 payload에 token/만료/식별자 넣어도 좋음)
            eventPublisher.publishEvent(
                    NotificationRequest.builder()
                            .notiType(NotiType.RENTAL_RESERVATION)
                            .userId(userId)
                            .build()
            );

            return rental;

        } catch (Exception e) {
            try {
                reservationRedisService.revertReserve(unitId, offerToken);
                eventPublisher.publishEvent(
                        NotificationRequest.builder()
                                .notiType(NotiType.HOLDING_CANCEL)
                                .userId(userId)
                                .build()
                );
            } catch (Exception revertEx) {
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
        if (r.isReservationExpired(LocalDateTime.now(Z))) throw new IllegalStateException("reservation expired");

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
            if (rental == null) return;

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
    @Transactional(readOnly = true)
    public Page<Rental> listMyHistory(
            Long u,
            Long userId,
            Set<RentalStatus> statuses,
            LocalDateTime from,
            LocalDateTime to,
            boolean includeExpiredReservations,
            Pageable pageable
    ) {
        Specification<Rental> spec = (root, query, cb) -> {
            var ands = new ArrayList<Predicate>();

            // tenant & owner
            ands.add(cb.equal(root.get("universityId"), u));
            ands.add(cb.equal(root.get("userId"), userId));

            // status filter
            if (statuses != null && !statuses.isEmpty()) {
                ands.add(root.get("status").in(statuses));
            }

            // exclude expired RESERVED when includeExpiredReservations == false
            if (!includeExpiredReservations) {
                var notExpiredReserved = cb.or(
                        cb.notEqual(root.get("status"), RentalStatus.RESERVED),
                        cb.greaterThan(root.get("reserveExpiresAt"), LocalDateTime.now())
                );
                ands.add(notExpiredReserved);
            }

            // period filter: any of the transition timestamps within [from, to]
            if (from != null || to != null) {
                var ors = new ArrayList<Predicate>();

                // helper lambdas
                var addRange = (java.util.function.BiConsumer<String, String>) (field, unused) -> {
                    if (from != null && to != null) {
                        ors.add(cb.between(root.get(field), from, to));
                    } else if (from != null) {
                        ors.add(cb.greaterThanOrEqualTo(root.get(field), from));
                    } else if (to != null) {
                        ors.add(cb.lessThanOrEqualTo(root.get(field), to));
                    }
                };

                addRange.accept("reservedAt", "");
                addRange.accept("rentedAt", "");
                addRange.accept("returnedAt", "");
                addRange.accept("cancelledAt", "");

                // reservedAt/rentedAt/... 가 전부 null일 수도 있으니, OR이 비어있지 않을 때만 추가
                if (!ors.isEmpty()) {
                    ands.add(cb.or(ors.toArray(new Predicate[0])));
                }
            }

            return cb.and(ands.toArray(new Predicate[0]));
        };

        return rentalRepository.findAll(spec, pageable);
    }
    @Transactional(readOnly = true)
    public Page<Rental> listMyCurrentRentals(Long u, Long userId, Pageable pageable) {
        // 반환처리 전까지는 status=REN​TED이므로 이 조건이면 충분
        return rentalRepository.findAllByUniversityIdAndUserIdAndStatus(
                u, userId, RentalStatus.RENTED, pageable
        );
    }
    @Transactional(readOnly = true)
    public Page<Rental> listMyActiveReservationsByOrganization(
            Long u, Long organizationId, Long userId, Pageable pageable
    ) {
        Specification<Rental> spec = (root, query, cb) -> {
            var ands = new java.util.ArrayList<Predicate>();

            ands.add(cb.equal(root.get("universityId"), u));
            ands.add(cb.equal(root.get("userId"), userId));
            ands.add(cb.equal(root.get("status"), RentalStatus.RESERVED));
            ands.add(cb.greaterThan(root.get("reserveExpiresAt"), LocalDateTime.now())); // 미만료

            // (A) Rental.organizationId = orgId
            Predicate pA = cb.equal(root.get("organizationId"), organizationId);

            // (B) Rental.item.organizationId = orgId (JOIN)
            var itemJoin = root.join("item", JoinType.LEFT);
            Predicate pB = cb.equal(itemJoin.get("organizationId"), organizationId);

            ands.add(cb.or(pA, pB)); // A 또는 B

            return cb.and(ands.toArray(new Predicate[0]));
        };

        return rentalRepository.findAll(spec, pageable);
    }
    @Transactional(readOnly = true)
    public Page<Rental> listMyCurrentRentalsByOrganization(
            Long u, Long organizationId, Long userId, Pageable pageable
    ) {
        Specification<Rental> spec = (root, query, cb) -> {
            var ands = new java.util.ArrayList<Predicate>();
            ands.add(cb.equal(root.get("universityId"), u));
            ands.add(cb.equal(root.get("userId"), userId));
            ands.add(cb.equal(root.get("status"), RentalStatus.RENTED));
            ands.add(cb.isNull(root.get("returnedAt"))); // 반납 전

            // (A) Rental.organizationId = orgId
            Predicate pA = cb.equal(root.get("organizationId"), organizationId);
            // (B) Item.organizationId = orgId
            var itemJoin = root.join("item", JoinType.LEFT);
            Predicate pB = cb.equal(itemJoin.get("organizationId"), organizationId);
            ands.add(cb.or(pA, pB));

            return cb.and(ands.toArray(new Predicate[0]));
        };
        return rentalRepository.findAll(spec, pageable);
    }

    @Transactional
    public void createTempRental(Rental rental) {
        rentalRepository.save(rental);
    }
    @Transactional(readOnly = true)
    public List<Rental> listMyActiveReservationsByOrganizationRaw(Long u, Long organizationId, Long userId) {
        return rentalRepository.findMyReservedActiveByOrg(u, userId, organizationId);
    }

    @Transactional(readOnly = true)
    public BigDecimal precheckAndGetDepositAmount(Long u, Long o, Long rentalId, Long actorUserId) {
        Rental r = rentalRepository.findByIdAndTenant(u, o, rentalId)
            .orElseThrow(() -> new NoSuchElementException("rental not found"));

        if (!r.getUserId().equals(actorUserId)) throw new IllegalStateException("not owner");
        if (r.getStatus() != RentalStatus.RESERVED) throw new IllegalStateException("not RESERVED");
        if (r.isReservationExpired(LocalDateTime.now(Z))) throw new IllegalStateException("reservation expired");

        BigDecimal depositAmount = Optional.ofNullable(
            r.getItem() != null ? r.getItem().getDeposit() : null
        ).orElseThrow(() -> new IllegalStateException("deposit amount missing"));

        return depositAmount;
    }

}