package com.joeun.service.waitlist;

import com.joeun.domain.notification.entity.NotiType;
import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.service.WaitlistRdsService;
import com.joeun.global.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WaitlistDomainService {

    private final WaitlistRdsService waitlistRdsService;
    private final ApplicationEventPublisher eventPublisher;

    public void joinWaitlist(Waitlist waitlist) {
        waitlistRdsService.joinWaitlist(waitlist);
    }

    public Optional<Waitlist> getNextOutstandingWaitlist(Long itemId) {
        return waitlistRdsService.getNextOutstandingWaitlist(itemId);
    }

    public void markNotified(Long id, LocalDateTime now) {
        Optional<Waitlist> waitlistOpt = waitlistRdsService.getNextOutstandingWaitlist(id);
        waitlistOpt.ifPresent(waitlist -> {
            waitlist.markNotified(now);
            waitlistRdsService.joinWaitlist(waitlist);
            eventPublisher.publishEvent(
                    NotificationRequest.builder()
                            .notiType(NotiType.WAITING_LIST_ESCAPE)
                            .userId(waitlist.getUser().getId())
                            .build());
        });
    }
}
