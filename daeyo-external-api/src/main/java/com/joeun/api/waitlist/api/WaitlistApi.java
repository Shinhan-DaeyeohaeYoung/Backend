package com.joeun.api.waitlist.api;

import com.joeun.global.config.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "대기열", description = "대기열 관련 API")
public interface WaitlistApi {

    @Operation(summary = "대기열 참여", description = "특정 아이템의 대기열에 참여합니다.")
    public ResponseEntity<Void> joinWaitListByItemId(@PathVariable @Valid Long itemId,
                                                     @AuthenticationPrincipal LoginUser loginUser);
}
