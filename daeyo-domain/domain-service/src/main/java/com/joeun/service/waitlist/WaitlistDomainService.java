package com.joeun.service.waitlist;

import com.joeun.domain.notification.entity.NotiType;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.entity.WaitlistStatus;
import com.joeun.domain.waitlist.service.WaitlistRdsService;
import com.joeun.global.dto.NotificationRequest;
import com.joeun.service.rental.RentalDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WaitlistDomainService {

    private final WaitlistRdsService waitlistRdsService;
    private final ApplicationEventPublisher eventPublisher;
    private final RentalDomainService rentalDomainService;

    public void joinWaitlist(Waitlist waitlist) {
        waitlistRdsService.joinWaitlist(waitlist);
    }

    public Optional<Waitlist> getNextOutstandingWaitlist(Long itemId) {
        return waitlistRdsService.getNextOutstandingWaitlist(itemId);
    }

    public void markNotified(Long id, LocalDateTime now, Long unitId) {
        Optional<Waitlist> waitlistOpt = waitlistRdsService.getNextOutstandingWaitlist(id);
        waitlistOpt.ifPresent(waitlist -> {
            waitlist.markNotified(now);
            waitlistRdsService.joinWaitlist(waitlist);
            eventPublisher.publishEvent(
                    NotificationRequest.builder()
                            .notiType(NotiType.WAITING_LIST_ESCAPE)
                            .userId(waitlist.getUser().getId())
                            .build());
            rentalDomainService.reserveUnit(
                    waitlist.getUser().getId(),
                    waitlist.getItem().getOrganizationId(),
                    waitlist.getUser().getId(),
                    waitlist.getItem().getId(),
                    unitId,
                    30
            );
            waitlist.offer(LocalDateTime.now());
        });
    }

    @Transactional
    public void offerReserveAndNotify(Long waitlistId,
                                      LocalDateTime now,
                                      Long unitId,
                                      Long u, Long o) {

        Waitlist waitlist = waitlistRdsService.findByIdForUpdate(waitlistId)
                .orElseThrow(() -> new NoSuchElementException("waitlist not found: " + waitlistId));

        Long userId = waitlist.getUser().getId();
        Long itemId = waitlist.getItem().getId();

        eventPublisher.publishEvent(
                NotificationRequest.builder()
                        .notiType(NotiType.WAITING_LIST_ESCAPE)
                        .userId(waitlist.getUser().getId())
                        .build());

        Rental rental = rentalDomainService.reserveUnit(
                u, o, userId, itemId, unitId, 30
        );

        waitlist.markNotified(now);
        waitlist.offer(now);
    }

    public int getWaitListCount(Long itemId) {
        return waitlistRdsService.getWaitListCount(itemId);
    }

    public void markFulfilledById(Long id, LocalDateTime now) {
        waitlistRdsService.markFulfilledById(id, now);
    }

    public List<Waitlist> getWaitlistsByItemIdAndStatus(Long itemId, WaitlistStatus waitlistStatus) {
        return waitlistRdsService.getWaitlistsByItemIdAndStatus(itemId, waitlistStatus);
    }

    public boolean existsActiveWait(Long itemId, Long id, List<WaitlistStatus> waiting) {
        return waitlistRdsService.existsActiveWait(itemId, id, waiting);
    }

    public Boolean existsWaitListByItemIdAndUserIdAndStatus(Long itemId, Long userId, WaitlistStatus waitlistStatus) {
        return waitlistRdsService.existsWaitListByItemIdAndUserIdAndStatus(itemId, userId, waitlistStatus);
    }

    public void markCancelledById(Long id, LocalDateTime now) {
        waitlistRdsService.markCancelledById(id, now);
    }
}
