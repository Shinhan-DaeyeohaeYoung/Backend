package com.joeun.domain.deposit.repository;

import com.joeun.domain.deposit.entity.Deposit;
import com.joeun.domain.deposit.entity.DepositEvent;
import com.joeun.domain.deposit.types.DepositEventType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepositEventRepository extends JpaRepository<DepositEvent, Long> {
  @Query("""
    select
      e.id as eventId,
      e.amount as amount,
      e.eventType as eventType,
      e.occurredAt as occurredAt,
      o.name as organizationName
    from DepositEvent e
      left join Organization o on o.id = e.organizationId
    where e.userId = :userId
      and (:eventType is null or e.eventType = :eventType)
    order by e.occurredAt desc, e.id desc
  """)
  List<DepositEventRow> findHistoryView(@Param("userId") Long userId,
      @Param("eventType") DepositEventType eventType);

  @Query("""
    select e.id as eventId,
           e.amount as amount,
           e.eventType as eventType,
           e.occurredAt as occurredAt,
           u.name as userName
    from DepositEvent e
      join e.deposit d
      join d.user u
    where e.organizationId = :orgId
      and (:eventType is null or e.eventType = :eventType)
    order by e.occurredAt desc, e.id desc
  """)
  List<OrgDepositEventRow> findOrgHistoryView(@Param("orgId") Long orgId,
      @Param("eventType") DepositEventType eventType);

}
