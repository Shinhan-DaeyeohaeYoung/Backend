package com.joeun.api.statistics.controller;

import com.joeun.scheduler.DailyItemRentalCountScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/statistics")
@Validated
public class StatisticsController {

    private final DailyItemRentalCountScheduler dailyItemRentalCountScheduler;

    @GetMapping("/daily-item-rental-count/run")
    public void runDailyItemRentalCountJob() {
        dailyItemRentalCountScheduler.runDailyItemRentalCountJob();
    }
}
