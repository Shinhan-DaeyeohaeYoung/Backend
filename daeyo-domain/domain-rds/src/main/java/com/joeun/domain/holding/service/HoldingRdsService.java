package com.joeun.domain.holding.service;

import com.joeun.domain.holding.entity.Holding;
import com.joeun.domain.holding.repository.HoldingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HoldingRdsService {

    private final HoldingRepository holdingRepository;

    @Transactional
    public void saveHolding(Holding holding) {
        holdingRepository.save(holding);
    }

    @Transactional(readOnly = true)
    public Holding findHoldingById(Long id) {
        return holdingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Holding not found with id: " + id));
    }

    @Transactional
    public void deleteHolding(Long id) {
        holdingRepository.deleteById(id);
    }
}
