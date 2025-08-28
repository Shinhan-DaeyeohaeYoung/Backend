// com.joeun.domain.item.service.UnitPhotoDomainService
package com.joeun.domain.item.service;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.entity.Item;
import com.joeun.domain.item.entity.UnitPhoto;
import com.joeun.domain.item.repository.IndividualItemRepository;
import com.joeun.domain.item.repository.ItemRepository;
import com.joeun.domain.item.repository.UnitPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnitPhotoDomainService {

    private final ItemRepository itemRepository;
    private final IndividualItemRepository unitRepository;
    private final UnitPhotoRepository photoRepository;

    private Item getItemOrThrow(Long u, Long o, Long itemId) {
        return itemRepository.findByIdAndUniversityIdAndOrganizationId(itemId, u, o)
                .orElseThrow(() -> new NoSuchElementException(
                        "item not found: id=%d, univ=%d, org=%d".formatted(itemId, u, o)));
    }

    private IndividualItem getUnitByAssetNoOrThrow(Item item, String assetNo) {
        return unitRepository.findByItemAndAssetNo(item, assetNo)
                .orElseThrow(() -> new NoSuchElementException("unit not found: assetNo=" + assetNo));
    }

    /* ===== 조회 ===== */

    @Transactional(readOnly = true)
    public Optional<UnitPhoto> findUnitCover(Long u, Long o, Long unitId) {
        return photoRepository.findByUniversityIdAndOrganizationIdAndUnit_Id(u, o, unitId);
    }
    @Transactional(readOnly = true)
    public Optional<UnitPhoto> findItemCover(Long u, Long o, Long itemId) {
        return photoRepository
                .findTopByUniversityIdAndOrganizationIdAndUnit_Item_IdOrderByTakenAtDescIdDesc(u, o, itemId);
    }

    @Transactional(readOnly = true)
    public List<UnitPhoto> listItemUnitPhotos(Long u, Long o, Long itemId) {
        return photoRepository.findAllByItem(u, o, itemId);
    }

    @Transactional(readOnly = true)
    public UnitPhoto getUnitPhotoByAssetNo(Long u, Long o, Long itemId, String assetNo) {
        return photoRepository.findByAssetNo(u, o, itemId, assetNo)
                .orElseThrow(() -> new NoSuchElementException("photo not found"));
    }

    /* ===== 생성/교체(업서트) & 삭제 ===== */

    @Transactional
    public Long upsertUnitPhotoByAssetNo(Long u, Long o, Long itemId, String assetNo,
                                         String key, String mime, String hash, LocalDateTime takenAt) {
        Item item = getItemOrThrow(u, o, itemId);
        IndividualItem unit = getUnitByAssetNoOrThrow(item, assetNo);

        var existing = photoRepository.findByUniversityIdAndOrganizationIdAndUnit_Id(u, o, unit.getId());
        if (existing.isPresent()) {
            existing.get().replace(key, mime, hash, takenAt);
            return existing.get().getId();
        } else {
            var saved = UnitPhoto.builder()
                    .universityId(u).organizationId(o)
                    .unit(unit)
                    .imageKey(key).mime(mime).hash(hash)
                    .takenAt(takenAt)
                    .build();
            photoRepository.save(saved);
            return saved.getId();
        }
    }

    @Transactional
    public void deleteUnitPhotoByAssetNo(Long u, Long o, Long itemId, String assetNo) {
        Item item = getItemOrThrow(u, o, itemId);
        IndividualItem unit = getUnitByAssetNoOrThrow(item, assetNo);
        photoRepository.deleteByUniversityIdAndOrganizationIdAndUnit_Id(u, o, unit.getId());
    }
}
