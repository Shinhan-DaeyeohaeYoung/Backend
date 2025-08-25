// com.joeun.domain.item.repository.UnitPhotoRepository
package com.joeun.domain.item.repository;

import com.joeun.domain.item.entity.UnitPhoto;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UnitPhotoRepository extends JpaRepository<UnitPhoto, Long> {
    // 개별상품(Unit) PK로 조회: unit.id 경로는 _Id 로 표현
    Optional<UnitPhoto> findByUnit_Id(Long unitId);

    // 멀티테넌트 안전장치(권장)
    Optional<UnitPhoto> findByUnit_IdAndUniversityIdAndOrganizationId(
            Long unitId, Long universityId, Long organizationId
    );
    // 아이템 기준 대표 사진 1장: takenAt DESC, id DESC 로 가장 최신
    Optional<UnitPhoto> findTopByUniversityIdAndOrganizationIdAndUnit_Item_IdOrderByTakenAtDescIdDesc(
            Long universityId, Long organizationId, Long itemId
    );
    /** 유닛 id로 단건 */
    Optional<UnitPhoto> findByUniversityIdAndOrganizationIdAndUnit_Id(Long u, Long o, Long unitId);

    /** 유닛 id로 삭제 */
    void deleteByUniversityIdAndOrganizationIdAndUnit_Id(Long u, Long o, Long unitId);

    /** 아이템 기준 커버(AVAILABLE 유닛 우선) */
    @Query("""
      select p from UnitPhoto p
      join p.unit u
      join u.item i
      where i.id = :itemId
        and i.universityId = :u
        and i.organizationId = :o
      order by case when u.status = 'AVAILABLE' then 0 else 1 end, p.id asc
    """)
    Optional<UnitPhoto> findCoverByItem(@Param("u") Long u, @Param("o") Long o, @Param("itemId") Long itemId);

    /** 아이템의 모든 유닛 사진(AVAILABLE 먼저) */
    @Query("""
      select p from UnitPhoto p
      join p.unit u
      join u.item i
      where i.id = :itemId
        and i.universityId = :u
        and i.organizationId = :o
      order by case when u.status = 'AVAILABLE' then 0 else 1 end, p.id asc
    """)
    List<UnitPhoto> findAllByItem(@Param("u") Long u, @Param("o") Long o, @Param("itemId") Long itemId);

    /** assetNo로 단건 조회 */
    @Query("""
      select p from UnitPhoto p
      join p.unit u
      join u.item i
      where i.id = :itemId
        and i.universityId = :u
        and i.organizationId = :o
        and u.assetNo = :assetNo
    """)
    Optional<UnitPhoto> findByAssetNo(@Param("u") Long u, @Param("o") Long o,
                                      @Param("itemId") Long itemId, @Param("assetNo") String assetNo);
}
