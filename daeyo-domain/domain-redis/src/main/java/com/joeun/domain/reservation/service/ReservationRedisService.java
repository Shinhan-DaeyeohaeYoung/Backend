package com.joeun.domain.reservation.service;

import com.joeun.domain.reservation.vo.ReserveResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationRedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisScript<List> holdingScript;
    private final RedisScript<List> revertHoldingScript;

    private static final String ITEM_PREFIX = "active:item:";
    private static final String HOLDING_PREFIX = "hold:";
    private static final String HOLDING_EXPIRATIONS = "hold:expirations";

    public ReserveResult doReserve(long itemId, String holdingId, int ttlSec) {

        List<String> keys = List.of(
                ITEM_PREFIX + itemId,
                HOLDING_PREFIX + holdingId,
                HOLDING_EXPIRATIONS
        );

        String[] args = { holdingId, String.valueOf(ttlSec) };

        List<?> result = stringRedisTemplate.execute(holdingScript, keys, args);

        if (result == null || result.isEmpty()) {
            throw new IllegalStateException("Lua execution failed or returned null");
        }

        long code = ((Number) result.get(0)).longValue();

        switch (Math.toIntExact(code)) {
            case 1:
                String expireEpoch = String.valueOf(result.get(1));
                return ReserveResult.builder()
                        .ok(true)
                        .expireEpoch(expireEpoch)
                        .reason(null)
                        .build();
            case 0:
                String reason = String.valueOf(result.get(1));
                return ReserveResult.builder()
                        .ok(false)
                        .expireEpoch(null)
                        .reason(reason)
                        .build();
        }

        throw new IllegalStateException("Unexpected result from Lua script: " + result);
    }

    public void revertReserve(long itemId, String holdingId) {
        List<String> keys = List.of(
                "active:item:" + itemId,
                "hold:" + holdingId,
                "hold:expirations"
        );
        String[] args = { holdingId };
        stringRedisTemplate.execute(revertHoldingScript, keys, args);
    }
}
