package com.joeun.api.statistics.controller;

import com.joeun.api.statistics.api.StatisticsApi;
import com.joeun.api.statistics.dto.ItemRentalCountResponse;
import com.joeun.api.statistics.service.StatisticsService;
import com.joeun.scheduler.DailyItemRentalCountScheduler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/statistics")
@Validated
public class StatisticsController implements StatisticsApi {

    private final DailyItemRentalCountScheduler dailyItemRentalCountScheduler;
    private final StatisticsService statisticsService;

    @GetMapping("/daily-item-rental-count/run")
    public void runDailyItemRentalCountJob() {
        dailyItemRentalCountScheduler.runDailyItemRentalCountJob();
    }

    @GetMapping("/{organizationId}/item-rental-count")
    public ResponseEntity<List<ItemRentalCountResponse>> getItemRentalCountsStatistics(
            @PathVariable @Valid Long organizationId, @RequestParam @Valid LocalDate startDate, @RequestParam @Valid LocalDate endDate) {
        return ResponseEntity.ok(
                statisticsService.getItemRentalCounts(organizationId, startDate, endDate)
        );
    }
}
