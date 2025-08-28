package com.joeun.service.returnrequest;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.UnitPhotoRepository;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import com.joeun.domain.rental.repository.RentalRepository;
import com.joeun.domain.reservation.service.ReservationRedisService;
import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import com.joeun.domain.returnrequest.repository.ReturnRequestRepository;
import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.service.rental.RentalDomainService;
import com.joeun.service.waitlist.WaitlistDomainService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.joeun.domain.item.entity.UnitPhoto;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReturnRequestService {

    private final ReturnRequestRepository returnRequestRepository;
    private final RentalRepository rentalRepository;
    private final UnitPhotoRepository unitPhotoRepository;
    private final IndividualItemRepository individualItemRepository;
    private final WaitlistDomainService waitlistDomainService;
    private final RentalDomainService rentalDomainService;
    private final ReservationRedisService reservationRedisService;


    /* ================== 조회 (관리자) ================== */

    @Transactional(readOnly = true)
    public Page<ReturnRequest> listForAdmin(Long universityId, Long organizationId,
                                            ReturnRequestStatus status, Pageable pageable) {
        if (status == null) {
            return returnRequestRepository.findAllByUniversityIdAndOrganizationId(universityId, organizationId, pageable);
        }
        return returnRequestRepository.findAllByUniversityIdAndOrganizationIdAndStatus(universityId, organizationId, status, pageable);
    }

    @Transactional(readOnly = true)
    public ReturnRequest detailForAdmin(Long universityId, Long organizationId, Long id) {
        return returnRequestRepository.findByIdAndUniversityIdAndOrganizationId(id, universityId, organizationId)
                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found"));
    }

    /* ================== 조회 (유저 본인) ================== */
    @Transactional(readOnly = true)
    public Page<ReturnRequest> listForUser(Long universityId, Long userId, Pageable pageable) {
        return returnRequestRepository.findAllByUniversityIdAndUserId(universityId, userId, pageable);
    }

    /* ================== 생성 (유저) ================== */
    @Transactional
    public ReturnRequest create(Long universityId,
                                Long organizationId,
                                Long userId,
                                Long rentalId,
                                String imageKey,
                                String imageMime,
                                String imageHash,
                                LocalDateTime takenAt) {

        // 1) 대여건 확인 (테넌트 일치 확인은 Rental 엔티티에 따라 보강)
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found"));

        // TODO: rental.getUniversityId()/getOrganizationId()가 있다면 일치 검증
        // if (!rental.getUniversityId().equals(universityId) || !rental.getOrganizationId().equals(organizationId)) ...

        // 2) 열린 신청 중복 방지 (서비스 레벨 보장)
        long activeCount = returnRequestRepository.countByRentalIdAndIsActiveTrue(rentalId);
        if (activeCount > 0) {
            throw new IllegalStateException("There is already an active return request for this rental");
        }

        // 3) 생성
        LocalDateTime now = LocalDateTime.now();
        ReturnRequest rr = ReturnRequest.create(
                universityId, organizationId, userId, rental,
                imageKey, imageMime, imageHash, takenAt, now
        );
        return returnRequestRepository.save(rr);
    }

    /* ================== 승인 (관리자) ================== */
    @Transactional
    public ReturnRequest approve(Long approverUserId, Long id) {
        Long u = 1L;
        Long o = 2L;

        ReturnRequest rr = returnRequestRepository.lockByIdAndTenant(id, u, o)
                .orElseThrow(() -> new NoSuchElementException(
                        "returnRequest not found: id=%d, univ=%d, org=%d".formatted(id, u, o)));

        LocalDateTime now = LocalDateTime.now();
        rr.approve(approverUserId, now);

        this.markRentalReturnedByReturnApproval(u, o, rr.getRental().getId(), approverUserId);

        Long unitId = rr.getRental().getUnit().getId();
        IndividualItem unit = individualItemRepository.findById(unitId)
                .orElseThrow(() -> new NoSuchElementException("unit not found: id=" + unitId));

        reservationRedisService.revertReserve(unitId, rr.getRental().getOfferToken());

        Optional<Waitlist> next = waitlistDomainService.getNextOutstandingWaitlist(rr.getRental().getItem().getId());
        if (next.isPresent()) {
            Waitlist w = next.get();

            unit.markWaitReserved();

            waitlistDomainService.offerReserveAndNotify(
                    w.getId(), now, unitId, u, o
            );

            waitlistDomainService.markFulfilledById(w.getId(), now);
            return rr;
        }

        unit.markAvailable();
        unit.getItem().returnOne();
        unit.getItem().activeOn();
        return rr;
    }



    /* ================== 취소 (유저) ================== */
    @Transactional
    public ReturnRequest cancel(Long id, Long actorUserId) {
        Long u = 1L;
        Long o = 2L;

        ReturnRequest rr = returnRequestRepository.lockByIdAndTenant(id, u, o)
                .orElseThrow(() -> new EntityNotFoundException(
                        "ReturnRequest not found: id=%d, univ=%d, org=%d".formatted(id, u, o)));

        rr.cancelByUser(actorUserId);
        return rr;
    }
    @Transactional(rollbackFor = Exception.class)
    public void markRentalReturnedByReturnApproval(Long u, Long o, Long rentalId, Long approverUserId) {
        var now = LocalDateTime.now();

        // RENTAL만 잠그고 상태 전이 — unit 건드리지 않음
        Rental r = rentalRepository.lockByIdAndTenant(u, o, rentalId)
                .orElseThrow(() -> new NoSuchElementException("rental not found"));

        // 정책: RENTED → RETURNED 만 허용 (이미 RETURNED면 무시)
        if (r.getStatus() == RentalStatus.RETURNED) {
            return; // idempotent
        }
        if (r.getStatus() != RentalStatus.RENTED) {
            throw new IllegalStateException("rental is not RENTED");
        }

        r.markReturned(now);

    }

    // 조직 ID를 모르는 유저 취소 케이스용(테넌트 테이블 구조에 맞춰 수정 가능)
    private Long getOrgIdOrZero() { return 0L; }


    //반납 전에 원래 등록돼 있던 사진키 불러오기
    private String extractBeforeImageKey(ReturnRequest rr) {
        if (rr == null || rr.getRental() == null) return null;
        Long unitId = rr.getRental().getUnit().getId();
        if (unitId == null) return null;

        Long u = 1L;
        Long o = 2L;

        return unitPhotoRepository
                .findByUnit_IdAndUniversityIdAndOrganizationId(unitId, u, o)
                .or(() -> unitPhotoRepository.findByUnit_Id(unitId))
                .map(UnitPhoto::getImageKey)
                .orElse(null);
    }
}