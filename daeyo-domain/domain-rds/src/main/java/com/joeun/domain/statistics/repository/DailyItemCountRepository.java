package com.joeun.domain.statistics.repository;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyItemCountRepository extends JpaRepository<DailyItemRentalCount, Long> {
}
