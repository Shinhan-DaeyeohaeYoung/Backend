package com.joeun.api.item.service;

import com.joeun.api.item.dto.AdminItemRegisterDtos;
import com.joeun.api.item.dto.AdminItemRegisterDtos.UnitBatchCreateRequest;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.ItemDtos.*;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.security.TenantProvider;
import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.service.ItemDomainService;
import com.joeun.domain.item.service.UnitPhotoDomainService;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.service.rental.RentalDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemApplicationService {

    private final ItemDomainService itemDomainService;
    private final UnitPhotoDomainService unitPhotoDomainService;
    private final TenantProvider tenant;
    private final RentalDomainService rentalDomainService;

    /* ---------- 공통: 상세 조회(관리자/사용자 동일 포맷) ---------- */
    public ItemDtos.ItemDetailResponse getItemDetail(Long itemId,
                                                     Pageable pageable,
                                                     boolean includeUnits,
                                                     boolean includeRentalBrief) {
        Long u = tenant.universityId(), o = tenant.organizationId();

        var item = itemDomainService.getByTenant(u, o, itemId);
        var stats = itemDomainService.unitStats(u, o, itemId);

        // 모든 유닛 사진
        var photos = unitPhotoDomainService.listItemUnitPhotos(u, o, itemId).stream()
                .map(p -> new ItemDtos.UnitPhotoSummary(
                        p.getUnit().getAssetNo(),
                        p.getImageKey()
                ))
                .toList();

        UnitPageResponse unitsDto = null;

        if (includeUnits) {
            Page<IndividualItem> unitsPage = itemDomainService.listUnits(u, o, itemId, pageable);

            Map<Long, ItemDtos.RentalBrief> briefMap = Map.of();
            if (includeRentalBrief && !unitsPage.isEmpty()) {
                var unitIds = unitsPage.getContent().stream().map(IndividualItem::getId).toList();

                List<Rental> rentals = rentalDomainService.findActiveByUnitIds(u, unitIds);
                final Map<Long, ItemDtos.RentalBrief> tmp = rentals.stream().collect(Collectors.toMap(
                        r -> r.getUnit().getId(),
                        r -> new ItemDtos.RentalBrief(r.getId(), r.getUserId(), r.getDueAt()),
                        (a, b) -> a
                ));
                briefMap = tmp; // effectively final 유지
            }

            final Map<Long, ItemDtos.RentalBrief> finalBriefMap = briefMap;
            var content = unitsPage.getContent().stream()
                    .map(u0 -> new ItemDtos.UnitPageResponse.UnitSummary(
                            u0.getId(),
                            itemId,
                            u0.getStatus().name(),
                            u0.getAssetNo(),
                            finalBriefMap.get(u0.getId())
                    ))
                    .toList();

            unitsDto = new ItemDtos.UnitPageResponse(
                    content, unitsPage.getNumber(), unitsPage.getSize(), unitsPage.getTotalElements()
            );
        }

        return new ItemDtos.ItemDetailResponse(
                item.getId(), item.getUniversityId(), item.getOrganizationId(),
                item.getName(), item.getDescription(),
                item.getDeposit(), item.getMaxRentalDays(),
                item.getTotalQuantity(), item.getAvailableQuantity(),
                item.getIsActive(),
                stats,
                photos,
                unitsDto
        );
    }
    /* 목록 */
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

    /* 사진 */
    public Long upsertUnitPhoto(Long itemId, String assetNo, UnitPhotoDtos.UpsertRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        LocalDateTime takenAt = (req.takenAt() == null ? LocalDateTime.now() : LocalDateTime.parse(req.takenAt()));
        return unitPhotoDomainService.upsertUnitPhotoByAssetNo(
                u, o, itemId, assetNo, req.key(), req.mime(), req.hash(), takenAt);
    }

    public void deleteUnitPhoto(Long itemId, String assetNo) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        unitPhotoDomainService.deleteUnitPhotoByAssetNo(u, o, itemId, assetNo);
    }

    /* 관리자: 생성/수정/유닛등록 */
    public Long createItem(AdminItemRegisterDtos.ItemCreateRequest req) {
        Long u = req.universityId() != null ? req.universityId() : tenant.universityId();
        Long o = req.organizationId() != null ? req.organizationId() : tenant.organizationId();
        return itemDomainService.createItem(
                u, o, req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive()
        ).getId();
    }

    public void patchItem(Long itemId, AdminItemRegisterDtos.ItemPatchRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();
        itemDomainService.patchItem(u, o, itemId,
                req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive());
    }

    public Map<String, Object> createUnits(Long itemId, UnitBatchCreateRequest req) {
        Long u = tenant.universityId(), o = tenant.organizationId();

        var createdAssetNos = itemDomainService.createUnits(
                u, o, itemId,
                req.units().stream()
                        .map(x -> new com.joeun.domain.item.service.ItemDomainService.UnitCreate(
                                x.assetNo(), x.description(), x.status()))
                        .toList()
        );

        for (var x : req.units()) {
            var p = x.photo();
            if (p != null && StringUtils.hasText(p.key())) {
                unitPhotoDomainService.upsertUnitPhotoByAssetNo(
                        u, o, itemId, x.assetNo(), p.key(), p.mime(), p.hash(), null);
            }
        }

        return Map.of("created", createdAssetNos.size(), "assetNos", createdAssetNos);
    }
    /** 유닛 단건 사진 조회 */
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
