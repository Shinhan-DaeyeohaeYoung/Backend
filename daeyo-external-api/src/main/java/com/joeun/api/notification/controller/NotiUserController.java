package com.joeun.api.notification.controller;

import com.joeun.api.notification.api.NotiUserApi;
import com.joeun.api.notification.dto.NotiUserDisableRequest;
import com.joeun.api.notification.dto.NotiUserRequest;
import com.joeun.api.notification.service.NotiUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification/users")
@Validated
public class NotiUserController implements NotiUserApi {

    private final NotiUserService notiUserService;

    @PostMapping
    public ResponseEntity<Void> createNotiUser(NotiUserRequest request) {
        notiUserService.createNotiUser(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/disable")
    public ResponseEntity<Void> disableNotiUser(@PathVariable @Valid Long userId, NotiUserDisableRequest request) {
        notiUserService.disableNotiUser(userId, request);
        return ResponseEntity.ok().build();
    }
}
