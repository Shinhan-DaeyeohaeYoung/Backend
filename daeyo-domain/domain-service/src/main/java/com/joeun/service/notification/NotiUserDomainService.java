package com.joeun.service.notification;

import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.service.NotiUserRdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotiUserDomainService {

    private final NotiUserRdsService notiUserRdsService;

    public void saveNotiUser(NotiUser notiUser) {
        notiUserRdsService.saveNotiUser(notiUser);
    }

    public NotiUser findNotiUserById(Long id) {
        return notiUserRdsService.findNotiUserById(id);
    }

    public List<NotiUser> findNotiUserByUserId(Long userId) {
        return notiUserRdsService.findNotiUserByUserId(userId);
    }

    public void deleteNotiUser(Long id) {
        notiUserRdsService.deleteNotiUser(id);
    }

    public void updateActiveStatus(Long userId, boolean isActive) {
        notiUserRdsService.updateActiveStatus(userId, isActive);
    }
}
