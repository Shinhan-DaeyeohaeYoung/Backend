package com.joeun.domain.holding.repository;

import com.joeun.domain.holding.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
}
