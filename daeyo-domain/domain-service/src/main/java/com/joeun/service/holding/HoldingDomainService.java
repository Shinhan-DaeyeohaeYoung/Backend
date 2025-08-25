package com.joeun.service.holding;

import com.joeun.domain.holding.service.HoldingRdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HoldingDomainService {

    private final HoldingRdsService holdingRdsService;


}
