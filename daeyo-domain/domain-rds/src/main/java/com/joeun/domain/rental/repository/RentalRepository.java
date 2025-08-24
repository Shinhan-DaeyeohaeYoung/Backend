package com.joeun.domain.rental.repository;

import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
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
}