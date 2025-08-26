package com.joeun.worker;

import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.rental.dto.ExpiredRentalRow;
import com.joeun.domain.rental.repository.RentalRepository;
import com.joeun.domain.reservation.service.ReservationRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryFallbackScheduler {

    private final TransactionTemplate tx;
    private final RentalRepository rentalRepository;
    private final IndividualItemRepository individualItemRepository;
    private final ReservationRedisService reservationRedisService;

    private static final int MAX_BATCH = 500;

    @Scheduled(cron = "0 */5 * * * *")
    public void holdExpireFallback() {
        int total = 0;

        while (true) {
            List<ExpiredRentalRow> processed = tx.execute(status -> {
                List<ExpiredRentalRow> rows = rentalRepository.lockExpiredBatch(MAX_BATCH);
                if (rows.isEmpty()) return rows;

                List<Long> ids = rows.stream().map(ExpiredRentalRow::getId).toList();
                List<Long> unitIds = rows.stream().map(ExpiredRentalRow::getUnitId).toList();

                int changed1 = rentalRepository.bulkExpire(ids);
                int changed2 = individualItemRepository.bulkMakeAvailable(unitIds);

                log.debug("fallback batch: rentals={}, units={}", changed1, changed2);
                return rows;
            });

            if (processed == null || processed.isEmpty()) break;
            total += processed.size();

            for (ExpiredRentalRow row : processed) {
                try {
                    reservationRedisService.cleanupReserve(row.getUnitId(), row.getOfferToken());
                } catch (Exception ex) {
                    // 커밋 후 구간 → 롤백 불가. 재시도 큐/로그 남기기
                    log.warn("cleanup after commit failed unitId={}, token={}",
                            row.getUnitId(), row.getOfferToken(), ex);
                }
            }

            if (processed.size() < MAX_BATCH) break;
        }

        if (total > 0) log.info("fallback sweep expired: {}", total);
    }
}

