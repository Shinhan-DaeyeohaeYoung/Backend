package com.joeun.writer;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dailyItemRentalCountWriter")
@StepScope
@RequiredArgsConstructor
public class DailyItemRentalCountWriter implements ItemWriter<DailyItemRentalCount>, ItemStream {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends DailyItemRentalCount> chunk) {
        String sql = """
            INSERT INTO daily_item_rental_count (statistics_date, organization_id, item_id, item_name, rental_count)
            VALUES (?, ?, ?, ?, ?)
            """;

        List<DailyItemRentalCount> items = List.copyOf((List<? extends DailyItemRentalCount>) chunk.getItems());

        jdbcTemplate.batchUpdate(sql, items, 1000, (ps, item) -> {
            ps.setObject(1, item.getStatisticsDate());
            ps.setLong(2, item.getOrganizationId());
            ps.setLong(3, item.getItemId());
            ps.setString(4, item.getItemName());
            ps.setInt(5, item.getRentalCount());
        });
    }
}

