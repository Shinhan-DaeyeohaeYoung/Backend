package com.joeun.port.notification;

import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;

public interface NotificationInfraService {
    void sendNotification(Notification notification, NotiUser notiUser);
}
