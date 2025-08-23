package com.joeun.api.item.service;

import com.joeun.api.item.dto.ItemDtos.*;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.item.entity.Item;
import com.joeun.domain.item.service.ItemDomainService;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemApplicationService {

    private final ItemDomainService itemDomainService;
    private final TenantProvider tenant;               // 대학/조직 ID 제공 (JWT 등)

    /* 사용자 조회 */
    public Page<ItemSummaryResponse> listForUser(Pageable pageable) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        return itemDomainService.listActive(u, o, pageable)
                .map(i -> new ItemDtos.ItemSummaryResponse(
                        i.getId(), i.getUniversityId(), i.getOrganizationId(),
                        i.getName(), i.getTotalQuantity(), i.getAvailableQuantity(), i.getIsActive()
                ));
    }

    public ItemDetailResponse getForUser(Long itemId) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        Item i = itemDomainService.getByTenant(u, o, itemId);
        Map<String, Long> stats = itemDomainService.unitStats(u, o, itemId);
        return new ItemDetailResponse(
                i.getId(), i.getUniversityId(), i.getOrganizationId(),
                i.getName(), i.getDescription(), i.getDeposit(), i.getMaxRentalDays(),
                i.getTotalQuantity(), i.getAvailableQuantity(), i.getIsActive(), stats
        );
    }

    /* 관리자 */
    public Long createItem(ItemCreateRequest req) {
        Long u = Optional.ofNullable(req.universityId()).orElseGet(tenant::universityId);
        Long o = Optional.ofNullable(req.organizationId()).orElseGet(tenant::organizationId);
        return itemDomainService
                .createItem(u, o, req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive())
                .getId();
    }

    public void patchItem(Long itemId, ItemPatchRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        itemDomainService.patchItem(u, o, itemId, req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive());
    }

    public Map<String, Object> createUnits(Long itemId, UnitBatchCreateRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var inputs = req.units().stream()
                .map(uReq -> new ItemDomainService.UnitCreate(uReq.assetNo(), uReq.description(), uReq.status()))
                .toList();

        var results = itemDomainService.createUnits(u, o, itemId, inputs);

        // 옵션 A) assetNo만 반환 (요청과 동일한 식별자)
        var assetNos = results.stream().map(ItemDomainService.UnitCreateResult::assetNo).toList();
        return Map.of("created", assetNos.size(), "assetNos", assetNos);
    }

        public Map<String, Object> adminDetail(Long itemId, Pageable pageable) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var detail = getForUser(itemId);
        var page = itemDomainService.listUnits(u, o, itemId, pageable);
        var content = page.map(it -> new ItemDtos.UnitPageResponse.UnitSummary(
                it.getId(), itemId, it.getStatus().name(), it.getAssetNo()
        )).toList();
        var units = new ItemDtos.UnitPageResponse(content, page.getNumber(), page.getSize(), page.getTotalElements());
        return Map.of("item", detail, "units", units);
    }
}
