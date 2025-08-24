package com.joeun.api.notification.service;

import com.joeun.api.notification.dto.NotiUserDisableRequest;
import com.joeun.api.notification.dto.NotiUserRequest;
import com.joeun.domain.notification.entity.DeviceInfo;
import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.service.notification.NotiUserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotiUserService {

    private final NotiUserDomainService notiUserDomainService;

    public void createNotiUser(NotiUserRequest request) {

        NotiUser notiUser = NotiUser.builder()
                .userId(request.userId())
                .deviceInfo(
                        DeviceInfo.builder()
                                .token(request.token())
                                .deviceType(request.deviceType())
                                .build()
                )
                .isActive(request.isActive())
                .build();

        notiUserDomainService.saveNotiUser(notiUser);
    }

    public void disableNotiUser(Long userId, NotiUserDisableRequest request) {
        notiUserDomainService.updateActiveStatus(
                userId,
                request.isActive());
    }
}
