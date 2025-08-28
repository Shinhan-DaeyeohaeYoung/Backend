package com.joeun.domain.returnrequest.repository;

import com.joeun.domain.returnrequest.entity.ReturnRequest;
import com.joeun.domain.returnrequest.entity.ReturnRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    Optional<ReturnRequest> findByIdAndUniversityIdAndOrganizationId(Long id, Long universityId, Long organizationId);

    Page<ReturnRequest> findAllByUniversityIdAndOrganizationId(Long universityId, Long organizationId, Pageable pageable);

    Page<ReturnRequest> findAllByUniversityIdAndUserId(Long universityId, Long userId, Pageable pageable);

    Page<ReturnRequest> findAllByUniversityIdAndOrganizationIdAndStatus(Long universityId, Long organizationId,
                                                                        ReturnRequestStatus status, Pageable pageable);

    long countByRentalIdAndIsActiveTrue(Long rentalId);

    List<ReturnRequest> findAllByRentalIdAndIsActiveTrue(Long rentalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rr from ReturnRequest rr where rr.id = :id and rr.universityId = :u and rr.organizationId = :o")
    Optional<ReturnRequest> lockByIdAndTenant(@Param("id") Long id, @Param("u") Long universityId, @Param("o") Long organizationId);

}
