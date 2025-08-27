package com.joeun.api.item.service;

import com.joeun.api.item.dto.AdminItemRegisterDtos.*;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.item.service.ItemDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminItemOrchestrator {
    private final ItemDomainService itemDomainService;

    public RegisterResponse registerWithUnits(RegisterRequest req) {
        Long u = req.universityId() ;
        Long o = req.organizationId();

        var item = itemDomainService.createItem(u, o, req.name(), req.description(), req.deposit(), req.maxRentalDays(), true);
        var unitInputs = req.units().stream()
                .map(uReq -> new ItemDomainService.UnitCreate(uReq.assetNo(), uReq.description(), uReq.status()))
                .toList();
        var unitIds = itemDomainService.createUnits(u, o, item.getId(), unitInputs);

        return new RegisterResponse(item.getId(), unitIds.size());
    }

}
