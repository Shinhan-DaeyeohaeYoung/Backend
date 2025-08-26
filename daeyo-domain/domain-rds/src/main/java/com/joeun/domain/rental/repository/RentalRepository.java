package com.joeun.domain.rental.repository;

import com.joeun.domain.rental.dto.ExpiredRentalRow;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    Page<Rental> findAllByUniversityIdAndUserIdAndStatusAndReserveExpiresAtAfter(
            Long universityId, Long userId, RentalStatus status, LocalDateTime now, Pageable pageable);

    @Query("""
           select r from Rental r
           where r.id = :id and r.universityId = :u and r.organizationId = :o
           """)
    Optional<Rental> findByIdAndTenant(@Param("u") Long universityId,
                                       @Param("o") Long organizationId,
                                       @Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select r from Rental r
           where r.id = :id and r.universityId = :u and r.organizationId = :o
           """)
    Optional<Rental> lockByIdAndTenant(@Param("u") Long universityId,
                                       @Param("o") Long organizationId,
                                       @Param("id") Long id);
    List<Rental> findByUnitIdInAndStatusIn(Collection<Long> unitIds,
                                           Collection<RentalStatus> statuses);

    // Todo: @QueryHints 적용 시 MySQL에서 동작하지 않을 수 있음 -> DB 설정 조작 필요
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Rental r where r.offerToken = :holdingId")
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Optional<Rental> findByOfferTokenForUpdate(String holdingId);

    @Query(value = """
        SELECT r.id          AS id,
               r.individual_item_id     AS individualItemId,
               r.offer_token AS offerToken
        FROM rental r
        WHERE r.status = 'RESERVED'
          AND r.reserve_expires_at <= NOW(6)
        ORDER BY r.reserve_expires_at, r.id
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<ExpiredRentalRow> lockExpiredBatch(@Param("limit") int limit);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE rental
        SET status = 'EXPIRED',
            expired_at = NOW(6)
        WHERE id IN (:ids)
          AND status = 'RESERVED'
          AND reserve_expires_at <= NOW(6)
        """, nativeQuery = true)
    int bulkExpire(@Param("ids") List<Long> ids);
}