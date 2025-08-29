package com.joeun.writer;

import com.joeun.domain.statistics.entity.DailyItemOverdueCount;
import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("dailyItemOverdueCountWriter")
@StepScope
@RequiredArgsConstructor
public class DailyItemOverdueCountWriter implements ItemWriter<DailyItemOverdueCount>, ItemStream {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(Chunk<? extends DailyItemOverdueCount> chunk) {
        String sql = """
            INSERT INTO daily_item_overdue_count (statistics_date, organization_id, item_id, item_name, overdue_count)
            VALUES (?, ?, ?, ?, ?)
            """;

        List<DailyItemOverdueCount> items = List.copyOf((List<? extends DailyItemOverdueCount>) chunk.getItems());

        jdbcTemplate.batchUpdate(sql, items, 1000, (ps, item) -> {
            ps.setObject(1, item.getStatisticsDate());
            ps.setLong(2, item.getOrganizationId());
            ps.setLong(3, item.getItemId());
            ps.setString(4, item.getItemName());
            ps.setInt(5, item.getOverdueCount());
        });
    }
}
