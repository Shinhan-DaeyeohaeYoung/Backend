package com.joeun.service.returnrequest;

import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.UnitPhotoRepository;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.repository.RentalRepository;
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
        // 그냥 임의 값 고정
        Long u = 1L;
        Long o = 2L;

        // 1) 반납 신청 건 가져오기
        ReturnRequest rr = returnRequestRepository.lockByIdAndTenant(id, u, o)
                .orElseThrow(() -> new NoSuchElementException(
                        "returnRequest not found: id=%d, univ=%d, org=%d".formatted(id, u, o)));

        // 2) 승인 처리
        rr.approve(approverUserId, LocalDateTime.now());

        // 2-1) 대기열 존재 시 대기열 유저 할당
        Optional<Waitlist> waitlist = waitlistDomainService.getNextOutstandingWaitlist(rr.getRental().getItem().getId());

        if(waitlist.isPresent()){
            Waitlist w = waitlist.get();
            waitlistDomainService.markNotified(w.getId(), LocalDateTime.now());
            return rr;
        }

        // 3) 개별 상품 AVAILABLE 처리
        Long unitId = rr.getRental().getUnit().getId();
        var unit = individualItemRepository.findById(unitId)
                .orElseThrow(() -> new NoSuchElementException("unit not found: id=" + unitId));
        unit.markAvailable();

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


    // 조직 ID를 모르는 유저 취소 케이스용(테넌트 테이블 구조에 맞춰 수정 가능)
    private Long getOrgIdOrZero() { return 0L; }

//    /* ================== 손상률/제안 (GPT) ================== */
//    @Transactional(readOnly = true)
//    public DamageSuggestionResult getDamageSuggestions(Long universityId, Long id, Long actorUserIdOrNull) {
//        ReturnRequest rr = returnRequestRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("ReturnRequest not found"));
//        // 권한 체크(관리자 or 본인): 필요 시 보강.
//
//        // 기존 사진(대여 당시 사진) & 반납 사진(제출 이미지)을 준비
//        String beforeImageKey = extractBeforeImageKey(rr); // rental/item/unit 등에서 꺼내는 로직 구현
//        String afterImageKey  = rr.getSubmittedImageKey();
//
//        return damageService.assessDamage(beforeImageKey, afterImageKey, rr.getUniversityId(), rr.getOrganizationId(), rr.getId());
//    }

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


//    /* ======= 결과 DTO (손상률) ======= */
//    public record DamageSuggestionResult(
//            double damageRate,              // 0.0 ~ 1.0
//            String summary,                 // 한 줄 요약
//            String[] notes,                 // 세부 참고사항
//            String suggestedCompensation    // 배상/정책 안내 제안
//    ) {}
}