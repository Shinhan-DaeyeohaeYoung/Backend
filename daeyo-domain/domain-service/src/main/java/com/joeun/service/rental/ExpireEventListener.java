package com.joeun.service.rental;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpireEventListener implements MessageListener {

    private static final String HOLD_PREFIX = "hold:";
    private static final String ACTIVE_UNIT_PREFIX = "active:unit:";
    private final RentalDomainService rentalDomainService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        try {
            if (expiredKey.startsWith(HOLD_PREFIX)) {
                String holdingId = expiredKey.substring(HOLD_PREFIX.length());
                rentalDomainService.onHoldExpired(holdingId);
            } else if (expiredKey.startsWith(ACTIVE_UNIT_PREFIX)) {
                log.debug("Ignore active-unit expired: {}", expiredKey);
            }
        } catch (Exception e) {
            log.error("Expire handler error. key={}", expiredKey, e);
        }
    }
}
