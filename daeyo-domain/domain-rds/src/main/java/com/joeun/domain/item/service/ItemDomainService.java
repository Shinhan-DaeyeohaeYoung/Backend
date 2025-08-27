package com.joeun.domain.item.service;

import com.joeun.domain.item.entity.*;
import com.joeun.domain.item.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemDomainService {
    public record UnitCreateResult(Long unitId, String assetNo) {}
    private final ItemRepository itemRepository;
    private final IndividualItemRepository unitRepository;

    /* ===== 아이템 ////// */



    @Transactional
    public Item createItem(Long u, Long o, String name, String desc, Long deposit, Integer maxDays, Boolean active) {
        Item item = Item.builder()
                .universityId(u).organizationId(o)
                .name(name).description(desc)
                .deposit(deposit).maxRentalDays(maxDays)
                .isActive(Boolean.TRUE.equals(active))
                .totalQuantity(0).availableQuantity(0)
                .build();
        return itemRepository.save(item);
    }

    @Transactional
    public Item patchItem(Long u, Long o, Long itemId, String name, String desc, Long deposit, Integer maxDays, Boolean active) {
        Item item = getByTenant(u, o, itemId);
        item.patch(name, desc, deposit, maxDays, active);
        return item;
    }

    @Transactional
    public void deleteItem(Long u, Long o, Long itemId) {
        Item item = getByTenant(u, o, itemId);
        itemRepository.delete(item); // 필요 시 소프트삭제로 교체
    }

    @Transactional(readOnly = true)
    public Page<Item> listActive(Long u, Long o, Pageable pageable) {
        return itemRepository.findAllByUniversityIdAndOrganizationIdAndIsActiveTrue(u, o, pageable);
    }

    @Transactional(readOnly = true)
    public Item getByTenant(Long u, Long o, Long itemId) {
        return itemRepository.findByIdAndUniversityIdAndOrganizationId(itemId, u, o)
                .orElseThrow(() -> new NoSuchElementException("item not found"));
    }

    /* ===== 유닛 ////// */

    @Transactional
    public List<UnitCreateResult> createUnits(Long u, Long o, Long itemId, List<UnitCreate> units) {
        Item item = getByTenant(u, o, itemId);

        // 요청 내 중복 assetNo 방지
        Set<String> dupCheck = new HashSet<>();
        for (UnitCreate uc : units) {
            if (!dupCheck.add(uc.assetNo()))
                throw new IllegalArgumentException("duplicate assetNo in request: " + uc.assetNo());
        }

        List<UnitCreateResult> results = new ArrayList<>();
        for (UnitCreate uc : units) {
            if (unitRepository.existsByItemAndAssetNo(item, uc.assetNo())) {
                throw new IllegalStateException("assetNo already exists: " + uc.assetNo());
            }
            IndividualItemStatus st = IndividualItemStatus.valueOf(uc.status());
            IndividualItem saved = unitRepository.save(IndividualItem.builder()
                    .item(item)
                    .assetNo(uc.assetNo())
                    .description(uc.description())
                    .status(st)
                    .build());

            // 재고 반영
            item.increaseStock(1);
            if (st != IndividualItemStatus.AVAILABLE) item.decreaseStock(1);

            results.add(new UnitCreateResult(saved.getId(), saved.getAssetNo()));
        }
        return results;
    }

    @Transactional(readOnly = true)
    public Page<IndividualItem> listUnits(Long u, Long o, Long itemId, Pageable pageable) {
        Item item = getByTenant(u, o, itemId);
        return unitRepository.findAllByItem(item, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> unitStats(Long u, Long o, Long itemId) {
        Item item = getByTenant(u, o, itemId);
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("AVAILABLE", unitRepository.countByItemAndStatus(item, IndividualItemStatus.AVAILABLE));
        stats.put("RESERVED",  unitRepository.countByItemAndStatus(item, IndividualItemStatus.RESERVED));
        stats.put("RENTED",    unitRepository.countByItemAndStatus(item, IndividualItemStatus.RENTED));
        stats.put("REPAIR",    unitRepository.countByItemAndStatus(item, IndividualItemStatus.REPAIR));
        stats.put("LOST",      unitRepository.countByItemAndStatus(item, IndividualItemStatus.LOST));
        stats.put("DISPOSED",  unitRepository.countByItemAndStatus(item, IndividualItemStatus.DISPOSED));
        return stats;
    }

    /* ===== 입력 타입(도메인용 간단 DTO) ////// */
    public record UnitCreate(String assetNo, String description, String status) { }


    //디비에서 수동 상태변경시에도 자동 업데이트
    @Transactional
    public void changeUnitStatusByAssetNo(Long u, Long o, Long itemId, String assetNo, String toStatus) {
        Item item = getByTenant(u, o, itemId);

        // 잠금 걸고 가져와서 동시성 이슈 방지
        IndividualItem unit = unitRepository.findWithLockByItemAndAssetNo(item, assetNo)
                .orElseThrow(() -> new NoSuchElementException("unit not found: assetNo=" + assetNo));

        IndividualItemStatus oldSt = unit.getStatus();
        IndividualItemStatus newSt = IndividualItemStatus.valueOf(toStatus);

        if (oldSt == newSt) return;

        // 상태 변경
        unit.changeStatus(newSt);

        // 집계 보정: AVAILABLE ↔︎ 비가용 전환만 가용 수량 증감
        boolean oldAvail = (oldSt == IndividualItemStatus.AVAILABLE);
        boolean newAvail = (newSt == IndividualItemStatus.AVAILABLE);
        if (oldAvail && !newAvail) {
            item.decreaseStock(1);
        } else if (!oldAvail && newAvail) {
            item.increaseStock(1);
        }
        // totalQuantity는 상태 변경으로는 변하지 않음(유닛 추가/삭제시에만 변경)
    }
    @Transactional(readOnly = true)
    public Page<Item> listActive(Long actorUserId, Set<Long> orgIds, Pageable pageable) {
        // 조회 자체는 orgIds 기준으로 수행
        return itemRepository.findAllByOrganizationIdInAndIsActiveTrue(orgIds, pageable);
    }

}
