package com.joeun.api.statistics.api;

import com.joeun.api.statistics.dto.ItemRentalCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "관리자 통계 API", description = "관리자 통계 관련 API")
public interface StatisticsApi {

    @Operation(summary = "조직별 기간별 대여 아이템 통계 조회", description = "조직별 기간별 대여 아이템 통계를 조회합니다.")
    public ResponseEntity<List<ItemRentalCountResponse>> getItemRentalCountsStatistics(
            @PathVariable @Valid Long organizationId, @RequestParam @Valid LocalDate startDate, @RequestParam @Valid LocalDate endDate);
}
