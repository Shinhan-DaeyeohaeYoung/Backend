package com.joeun.domain.notification.service;

import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.repository.NotiUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotiUserRdsService {

    private final NotiUserRepository notiUserRepository;

    @Transactional
    public void saveNotiUser(NotiUser notiUser) {
        notiUserRepository.save(notiUser);
    }

    @Transactional(readOnly = true)
    public NotiUser findNotiUserById(Long id) {
        return notiUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NotiUser not found with id: " + id));
    }

    @Transactional
    public void deleteNotiUser(Long id) {
        NotiUser notiUser = findNotiUserById(id);
        notiUserRepository.delete(notiUser);
    }

    @Transactional(readOnly = true)
    public NotiUser findNotiUserByUserId(Long userId) {
        return notiUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("NotiUser not found with userId: " + userId));
    }

    @Transactional
    public void updateActiveStatus(Long userId, boolean isActive) {
        Optional<NotiUser> notiUser = notiUserRepository.findByUserId(userId);
        if (notiUser.isEmpty()) {
            throw new IllegalArgumentException("NotiUser not found for userId: " + userId);
        }
        notiUser.get().setActive(isActive);
    }
}
