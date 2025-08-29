package com.joeun.domain.statistics.repository;

import com.joeun.domain.statistics.entity.DailyItemOverdueCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyItemOverdueCountRepository extends JpaRepository<DailyItemOverdueCount, Long> {
}
