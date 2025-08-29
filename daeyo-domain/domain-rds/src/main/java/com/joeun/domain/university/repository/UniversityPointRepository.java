package com.joeun.domain.university.repository;

import com.joeun.domain.university.entity.UniversityPoint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UniversityPointRepository extends JpaRepository<UniversityPoint, Long> {
  Optional<UniversityPoint> findByUniversityId(Long universityId);
  List<UniversityPoint> findTop10ByOrderByPointDesc();

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
      INSERT INTO university_point (university_id, point, updated_at)
      VALUES (:univId, :delta, NOW(6))
      ON DUPLICATE KEY UPDATE
        point = point + VALUES(point),
        updated_at = VALUES(updated_at)
      """, nativeQuery = true)
  int upsertAdd(@Param("univId") Long universityId, @Param("delta") long delta);

  @Query(value = """
    SELECT 1 + (
      SELECT COUNT(*)
      FROM university_point up2
      WHERE up2.point > up.point
    ) AS rnk
    FROM university_point up
    WHERE up.university_id = :univId
    """, nativeQuery = true)
  Integer findRankByUniversityId(@Param("univId") Long universityId);
}
