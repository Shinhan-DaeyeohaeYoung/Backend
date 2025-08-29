package com.joeun.domain.notification.repository;

import com.joeun.domain.notification.entity.NotiUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotiUserRepository extends JpaRepository<NotiUser, Long>{
    Optional<NotiUser> findByUserId(Long userId);

    List<NotiUser> findAllByUserId(Long userId);
}
