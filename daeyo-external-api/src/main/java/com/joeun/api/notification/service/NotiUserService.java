package com.joeun.api.notification.service;

import com.joeun.api.notification.dto.NotiUserDisableRequest;
import com.joeun.api.notification.dto.NotiUserRequest;
import com.joeun.domain.notification.entity.DeviceInfo;
import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.users.entity.User;
import com.joeun.service.notification.NotiUserDomainService;
import com.joeun.service.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotiUserService {

    private final NotiUserDomainService notiUserDomainService;
    private final UserDomainService userDomainService;

    public void createNotiUser(NotiUserRequest request) {

        User user = userDomainService.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.userId()));

        NotiUser notiUser = NotiUser.builder()
                .user(user)
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
