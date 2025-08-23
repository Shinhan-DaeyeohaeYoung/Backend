package com.joeun.api.item.service;

import com.joeun.api.item.dto.AdminItemRegisterDtos;
import com.joeun.api.item.dto.ItemDtos.*;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.security.TenantProvider;
import com.joeun.api.item.dto.AdminItemRegisterDtos.UnitBatchCreateRequest;
import org.springframework.util.StringUtils;

import com.joeun.domain.item.service.ItemDomainService;
import com.joeun.domain.item.service.UnitPhotoDomainService;
import lombok.RequiredArgsConstructor;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemApplicationService {

    private final ItemDomainService itemDomainService;
    private final UnitPhotoDomainService unitPhotoDomainService;
    private final TenantProvider tenant;               // 대학/조직 ID 제공 (JWT 등)

    public Long upsertUnitPhoto(Long itemId, String assetNo, UnitPhotoDtos.UpsertRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var takenAt = (req.takenAt() == null ? java.time.LocalDateTime.now() : java.time.LocalDateTime.parse(req.takenAt()));
        return unitPhotoDomainService.upsertUnitPhotoByAssetNo(u, o, itemId, assetNo,
                req.key(), req.mime(), req.hash(), takenAt);
    }

    //사진 삭제
    public void deleteUnitPhoto(Long itemId, String assetNo) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        unitPhotoDomainService.deleteUnitPhotoByAssetNo(u, o, itemId, assetNo);
    }

    /* 사용자 조회 */
    public Page<ItemDtos.ItemSummaryResponse> listForUser(Pageable pageable) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        return itemDomainService.listActive(u, o, pageable)
                .map(i -> {
                    var cover = unitPhotoDomainService.findItemCover(u, o, i.getId()).orElse(null);
                    return new ItemDtos.ItemSummaryResponse(
                            i.getId(), i.getUniversityId(), i.getOrganizationId(),
                            i.getName(),
                            i.getTotalQuantity(), i.getAvailableQuantity(),
                            i.getIsActive(),
                            cover == null ? null : cover.getImageKey()
                    );
                });
    }

    public ItemDtos.ItemDetailResponse getForUser(Long itemId) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var item = itemDomainService.getByTenant(u, o, itemId);
        var stats = itemDomainService.unitStats(u, o, itemId);

        // 아이템의 모든 유닛 사진(AVAILABLE 우선)
        var photos = unitPhotoDomainService.listItemUnitPhotos(u, o, itemId).stream()
                .map(p -> new ItemDtos.UnitPhotoSummary(
                        p.getUnit().getAssetNo(),   // 연관 통해 assetNo 접근
                        p.getImageKey()
                ))
                .toList();

        return new ItemDtos.ItemDetailResponse(
                item.getId(), item.getUniversityId(), item.getOrganizationId(),
                item.getName(), item.getDescription(),
                item.getDeposit(), item.getMaxRentalDays(),
                item.getTotalQuantity(), item.getAvailableQuantity(),
                item.getIsActive(),
                stats,
                photos
        );
    }

    /* 관리자 */
    public Long createItem(AdminItemRegisterDtos.ItemCreateRequest req) {
        Long u = req.universityId() != null ? req.universityId() : tenant.universityId();
        Long o = req.organizationId() != null ? req.organizationId() : tenant.organizationId();

        var saved = itemDomainService.createItem(
                u, o,
                req.name(), req.description(),
                req.deposit(), req.maxRentalDays(),
                req.isActive()
        );
        return saved.getId();
    }

    public void patchItem(Long itemId, AdminItemRegisterDtos.ItemPatchRequest req) {
        Long u = tenant.universityId();
        Long o = tenant.organizationId();
        itemDomainService.patchItem(
                u, o, itemId,
                req.name(), req.description(),
                req.deposit(), req.maxRentalDays(),
                req.isActive()
        );
    }


    public Map<String, Object> createUnits(Long itemId, UnitBatchCreateRequest req) {  // ✅ Admin*
        Long u = tenant.universityId(), o = tenant.organizationId();

        var createdAssetNos = itemDomainService.createUnits(
                u, o, itemId,
                req.units().stream()
                        .map(x -> new com.joeun.domain.item.service.ItemDomainService.UnitCreate(
                                x.assetNo(), x.description(), x.status()
                        ))
                        .toList()
        );

        for (AdminItemRegisterDtos.UnitCreate x : req.units()) {  // ✅ 명시적으로 Admin*
            var p = x.photo();
            if (p != null && StringUtils.hasText(p.key())) {
                unitPhotoDomainService.upsertUnitPhotoByAssetNo(
                        u, o, itemId, x.assetNo(),
                        p.key(), p.mime(), p.hash(), null
                );
            }
        }

        return Map.of("created", createdAssetNos.size(), "assetNos", createdAssetNos);
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

    //유닛 단건 사진 조회
    public UnitPhotoDtos.DetailResponse getUnitPhoto(Long itemId, String assetNo) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        var p = unitPhotoDomainService.getUnitPhotoByAssetNo(u, o, itemId, assetNo);
        return new UnitPhotoDtos.DetailResponse(
                p.getId(),
                p.getImageKey(),
                p.getMime(),
                p.getHash(),
                p.getTakenAt() == null ? null : p.getTakenAt().toString()
                );
    }
}
