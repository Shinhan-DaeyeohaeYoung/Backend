package com.joeun.domain.item.repository;

import com.joeun.domain.item.entity.Item;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findAllByUniversityIdAndOrganizationIdAndIsActiveTrue(Long u, Long o, Pageable pageable);
    Optional<Item> findByIdAndUniversityIdAndOrganizationId(Long id, Long u, Long o);
}
