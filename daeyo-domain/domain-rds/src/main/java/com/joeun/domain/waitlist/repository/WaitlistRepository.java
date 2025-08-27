package com.joeun.domain.waitlist.repository;

import com.joeun.domain.waitlist.entity.Waitlist;
import com.joeun.domain.waitlist.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    @Query(value = """
      SELECT *
      FROM waitlist
      WHERE item_id = :itemId AND status = 'WAITING'
      ORDER BY priority ASC, joined_at ASC, id ASC
      LIMIT 1
      FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    Optional<Waitlist> findByItemIdAndStatusWAITINGOne(@Param("itemId") Long itemId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
      update Waitlist w
         set w.status = :offered,
             w.offeredAt = :now,
             w.offerExpiresAt = :expiresAt
       where w.id = :id
         and w.status = :waiting
    """)
    int offerIfWaiting(@Param("id") Long id,
                       @Param("now") LocalDateTime now,
                       @Param("expiresAt") LocalDateTime expiresAt,
                       @Param("offered") WaitlistStatus offered,
                       @Param("waiting") WaitlistStatus waiting);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Waitlist w
       set w.notifiedAt = :now
     where w.id = :id
       and w.status = com.joeun.domain.waitlist.entity.WaitlistStatus.OFFERED
       and w.notifiedAt is null
  """)
    int markNotifiedIfOffered(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM waitlist WHERE id = :id FOR UPDATE", nativeQuery = true)
    Optional<Waitlist> findByIdForUpdate(@Param("id") Long id);
}
