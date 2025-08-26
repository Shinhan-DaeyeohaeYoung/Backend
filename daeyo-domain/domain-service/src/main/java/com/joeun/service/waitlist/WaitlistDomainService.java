package com.joeun.service.waitlist;

import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.service.WaitlistRdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WaitlistDomainService {

    private final WaitlistRdsService waitlistRdsService;

    public void joinWaitlist(Waitlist waitlist) {
        waitlistRdsService.joinWaitlist(waitlist);
    }

    public Optional<Waitlist> getNextOutstandingWaitlist(Long itemId) {
        return waitlistRdsService.getNextOutstandingWaitlist(itemId);
    }

    public void markNotified(Long id, LocalDateTime now) {
        Optional<Waitlist> waitlistOpt = waitlistRdsService.getNextOutstandingWaitlist(id);
        waitlistOpt.ifPresent(waitlist -> {
            // Assuming Waitlist has a method to update its status and notifiedAt
            waitlist.markNotified(now);
            waitlistRdsService.joinWaitlist(waitlist); // Save the updated waitlist
        });
    }
}
