package com.joeun.reader;

import com.joeun.domain.rental.dto.RentalCountProjection;
import com.joeun.domain.rental.repository.RentalRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@StepScope
@Component("dailyItemRentalCountReader")
public class DailyItemRentalCountReader implements ItemReader<RentalCountProjection>, ItemStream {

    private final LocalDate statDate;
    private final Long fromId;
    private final Long toId;
    private final RentalRepository rentalRepository;

    private List<RentalCountProjection> buffer = Collections.emptyList();
    private int cursor = 0;

    public DailyItemRentalCountReader(
            @Value("#{T(java.time.LocalDate).parse(jobParameters['statDate'])}") LocalDate statDate,
            @Value("#{stepExecutionContext['fromId']}") Long fromId,
            @Value("#{stepExecutionContext['toId']}") Long toId,
            RentalRepository rentalRepository
    ) {
        this.statDate = statDate;
        this.fromId = fromId;
        this.toId = toId;
        this.rentalRepository = rentalRepository;
    }

    @Override
    public void open(ExecutionContext ctx) throws ItemStreamException {
        LocalDateTime start = statDate.atStartOfDay();
        LocalDateTime end = statDate.plusDays(1).atStartOfDay();

        this.buffer = rentalRepository
                .batchCountByOrganizationIdBetweenAndRentedAtBetween(fromId, toId, start, end);

        this.cursor = ctx.containsKey("dailyReader.cursor") ? ctx.getInt("dailyReader.cursor") : 0;
        if (cursor > buffer.size()) cursor = 0;
    }

    @Override
    public RentalCountProjection read() {
        if (cursor >= buffer.size()) return null;
        return buffer.get(cursor++);
    }

    @Override
    public void update(ExecutionContext ctx) throws ItemStreamException {
        ctx.putInt("dailyReader.cursor", cursor);
    }

    @Override
    public void close() throws ItemStreamException {
        buffer = Collections.emptyList();
    }
}