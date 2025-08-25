package com.joeun.api.notification.api;

import com.joeun.api.notification.dto.NotiUserDisableRequest;
import com.joeun.api.notification.dto.NotiUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "알림 사용자 API", description = "알림 사용자 관련 API")
public interface NotiUserApi {

    @Operation(summary = "알림 사용자 생성", description = "새로운 알림 사용자를 생성합니다.")
    public ResponseEntity<Void> createNotiUser(NotiUserRequest request);

    @Operation(summary = "알림 사용자 비활성화", description = "사용자의 알림을 비활성화합니다.")
    public ResponseEntity<Void> disableNotiUser(@PathVariable @Valid Long userId, NotiUserDisableRequest request);
}
