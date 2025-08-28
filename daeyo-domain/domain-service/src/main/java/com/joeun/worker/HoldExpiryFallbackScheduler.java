package com.joeun.worker;

import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.ItemRepository;
import com.joeun.domain.rental.dto.ExpiredRentalRow;
import com.joeun.domain.rental.repository.RentalRepository;
import com.joeun.domain.reservation.service.ReservationRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HoldExpiryFallbackScheduler {

    private final TransactionTemplate tx;
    private final RentalRepository rentalRepository;
    private final IndividualItemRepository individualItemRepository;
    private final ReservationRedisService reservationRedisService;
    private final ItemRepository itemRepository;

    private static final int MAX_BATCH = 500;
    private static final ZoneId Z = ZoneOffset.UTC;

    @Scheduled(cron = "0 */5 * * * *")
    public void holdExpireFallback() {
        int total = 0;

        while (true) {
            List<ExpiredRentalRow> processed = tx.execute(status -> {
                List<ExpiredRentalRow> rows = rentalRepository.lockExpiredBatch(MAX_BATCH, LocalDateTime.now(Z));
                if (rows.isEmpty()) return rows;

                List<Long> ids = rows.stream().map(ExpiredRentalRow::getId).toList();
                List<Long> individualItemIds = rows.stream().map(ExpiredRentalRow::getIndividualItemId).toList();
                List<Long> itemIds = rows.stream().map(ExpiredRentalRow::getItemId).distinct().toList();

                int changed1 = rentalRepository.bulkExpire(ids);
                int changed2 = individualItemRepository.bulkMakeAvailable(individualItemIds);
                int changed3 = itemRepository.bulkRecoverItems(itemIds);

                log.debug("fallback batch: rentals={}, units={}, items={}", changed1, changed2, changed3);
                return rows;
            });

            if (processed == null || processed.isEmpty()) break;
            total += processed.size();

            for (ExpiredRentalRow row : processed) {
                try {
                    reservationRedisService.cleanupReserve(row.getIndividualItemId(), row.getOfferToken());
                } catch (Exception ex) {
                    log.warn("cleanup after commit failed unitId={}, token={}",
                            row.getIndividualItemId(), row.getOfferToken(), ex);
                }
            }

            if (processed.size() < MAX_BATCH) break;
        }

        if (total > 0) log.info("fallback sweep expired: {}", total);
    }
}

