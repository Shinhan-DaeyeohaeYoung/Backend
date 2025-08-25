package com.joeun.domain.item.repository;

import com.joeun.domain.item.entity.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IndividualItemRepository extends  JpaRepository<IndividualItem, Long>{
    Page<IndividualItem> findAllByItem(Item item, Pageable pageable);
    long countByItemAndStatus(Item item, IndividualItemStatus status);

    boolean existsByItemAndAssetNo(Item item, String assetNo);
    Optional<IndividualItem> findByItemAndAssetNo(Item item, String assetNo);

    long countByItem(Item item);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IndividualItem> findWithLockByItemAndAssetNo(Item item, String assetNo);

    /** 유닛 행을 잠그고(비관적 락) 테넌트 경계로 조회 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
      select u from IndividualItem u
      where u.id = :unitId
        and u.item.universityId = :u
        and u.item.organizationId = :o
      """)
    Optional<IndividualItem> lockByIdAndTenant(@Param("u") Long universityId,
                                               @Param("o") Long organizationId,
                                               @Param("unitId") Long unitId);

    @Query("""
      select u from IndividualItem u
      where u.id = :unitId
        and u.item.universityId = :u
        and u.item.organizationId = :o
      """)
    Optional<IndividualItem> findByIdAndTenant(Long u, Long o, Long unitId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update IndividualItem i
         set i.status = :toStatus
       where i.id = :unitId
         and i.status = :fromStatus
    """)
    int updateStatusIfAvailable(Long unitId, IndividualItemStatus individualItemStatus, IndividualItemStatus individualItemStatus1);
}
