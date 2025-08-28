package com.joeun.api.notification.controller;

import com.joeun.api.notification.api.NotificationApi;
import com.joeun.api.notification.dto.NotificationReadMarkRequest;
import com.joeun.api.notification.dto.NotificationResponse;
import com.joeun.api.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
@Validated
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable Long userId) {
        List<NotificationResponse> responses = notificationService.getNotificationsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{userId}/read")
    public ResponseEntity<Void> markNotificationsAsRead(@PathVariable Long userId,
                                                        @RequestBody @Valid NotificationReadMarkRequest request) {
        notificationService.markNotificationsAsRead(userId, request);
        return ResponseEntity.ok().build();
    }
}
