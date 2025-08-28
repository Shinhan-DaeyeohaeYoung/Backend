package com.joeun.api.notification.api;

import com.joeun.api.notification.dto.NotificationReadMarkRequest;
import com.joeun.api.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "알림 API", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(summary = "사용자 알림 조회", description = "특정 사용자의 알림 목록을 조회합니다.")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable Long userId);

    @Operation(summary = "알림 읽음 처리", description = "특정 사용자의 알림을 읽음 처리합니다.")
    public ResponseEntity<Void> markNotificationsAsRead(@PathVariable Long userId,
                                                        @RequestBody @Valid NotificationReadMarkRequest request);
}
