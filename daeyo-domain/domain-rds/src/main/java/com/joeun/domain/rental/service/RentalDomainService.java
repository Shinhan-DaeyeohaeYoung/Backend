package com.joeun.domain.rental.service;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.entity.IndividualItemStatus;
import com.joeun.domain.item.entity.Item;
import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.ItemRepository;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import com.joeun.domain.rental.repository.RentalRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RentalDomainService {

    private final ItemRepository itemRepository;
    private final IndividualItemRepository unitRepository;
    private final RentalRepository rentalRepository;

    /** 1) 예약 생성: IndividualItem → RESERVED, Rental(RESERVED) 생성 */
    @Transactional
    public Long reserveUnit(Long u, Long o, Long userId, Long itemId, Long unitId, int ttlMinutes) {
        Item item = itemRepository.findByIdAndUniversityIdAndOrganizationId(itemId, u, o)
                .orElseThrow(() -> new NoSuchElementException("item not found"));

        IndividualItem unit = unitRepository.lockByIdAndTenant(u, o, unitId)
                .orElseThrow(() -> new NoSuchElementException("unit not found"));

        if (!unit.getItem().getId().equals(item.getId()))
            throw new IllegalStateException("unit not in this item");

        if (unit.getStatus() != IndividualItemStatus.AVAILABLE)
            throw new IllegalStateException("unit is not AVAILABLE");

        // 유닛 상태: AVAILABLE → RESERVED
        unit.changeStatus(IndividualItemStatus.RESERVED);

        // rental 생성: RESERVED 상태로
        var now = LocalDateTime.now();
        var expires = now.plusMinutes(ttlMinutes);

        Rental rental = Rental.builder()
                .universityId(u).organizationId(o)
                .userId(userId)
                .item(item)
                .unit(unit)
                .quantity(1)
                .status(RentalStatus.RESERVED)
                .reservedAt(now)
                .reserveExpiresAt(expires)
                .build();

        return rentalRepository.save(rental).getId();
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

        // ✅ 리포지토리/엔티티를 밖으로 노출하지 말고 스냅샷만 리턴
        return new Approved(r.getId(), r.getStatus(), r.getDueAt());
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