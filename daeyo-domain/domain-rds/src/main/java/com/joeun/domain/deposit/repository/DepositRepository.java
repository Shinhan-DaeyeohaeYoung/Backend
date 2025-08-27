package com.joeun.domain.deposit.repository;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.types.DepositStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepositRepository extends JpaRepository<Deposit, Long> {

  Page<Deposit> findByUserIdAndStatus(Long userId, DepositStatus status, Pageable pageable);

  @Query("""
     select d
       from Deposit d
      where d.organization.id = :orgId
        and (:hasStatuses = false or d.status in :statuses)
      order by d.createdAt desc
  """)
  List<Deposit> findByOrganization(@Param("orgId") Long orgId,
      @Param("hasStatuses") boolean hasStatuses,
      @Param("statuses") Collection<DepositStatus> statuses);

  @Query("""
      select d from Deposit d
        left join fetch d.university u
        left join fetch d.organization o
        left join fetch d.user usr
        left join fetch d.refundAccount ra
      where d.id = :id
      """)
  Optional<Deposit> findByIdWithAllJoins(Long id);

}
