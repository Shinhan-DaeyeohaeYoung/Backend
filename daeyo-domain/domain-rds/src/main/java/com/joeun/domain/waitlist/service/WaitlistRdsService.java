package com.joeun.domain.waitlist.service;

import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WaitlistRdsService {

    private final WaitlistRepository waitlistRepository;

    @Transactional
    public void joinWaitlist(Waitlist waitlist) {
        waitlistRepository.save(waitlist);
    }

    @Transactional
    public Optional<Waitlist> getNextOutstandingWaitlist(Long itemId) {
        return waitlistRepository.findByItemIdAndStatusWAITINGOne(itemId);
    }

    public Optional<Waitlist> findByIdForUpdate(Long waitlistId) {
        return waitlistRepository.findByIdForUpdate(waitlistId);
    }

    public int getWaitListCount(Long itemId) {
        return waitlistRepository.countWaitingByItemId(itemId);
    }

    public void markFulfilledById(Long id, LocalDateTime now) {
        waitlistRepository.markFulfilledById(id);
    }
}
