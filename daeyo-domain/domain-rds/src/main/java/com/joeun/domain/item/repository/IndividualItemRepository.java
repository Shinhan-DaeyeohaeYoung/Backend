package com.joeun.domain.item.repository;

import com.joeun.domain.item.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface IndividualItemRepository extends  JpaRepository<IndividualItem, Long>{
    Page<IndividualItem> findAllByItem(Item item, Pageable pageable);
    long countByItemAndStatus(Item item, IndividualItemStatus status);

    boolean existsByItemAndAssetNo(Item item, String assetNo);
    Optional<IndividualItem> findByItemAndAssetNo(Item item, String assetNo);

    long countByItem(Item item);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IndividualItem> findWithLockByItemAndAssetNo(Item item, String assetNo);
}
