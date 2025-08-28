package com.joeun.api.waitlist.api;

import com.joeun.api.waitlist.dto.WaitlistPositionByItemResponse;
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

    @Operation(summary = "대기열 현황 조회", description = "특정 아이템의 대기열 현황을 조회합니다.")
    public ResponseEntity<WaitlistPositionByItemResponse> getWaitlistCountByItemId(@PathVariable @Valid Long itemId,
                                                                                   @AuthenticationPrincipal LoginUser loginUser);

    @Operation(summary = "대기열 취소", description = "특정 아이템의 대기열 참여를 취소합니다.")
    public ResponseEntity<Void> cancelWaitlistByItemId(@PathVariable @Valid Long itemId,
                                                       @AuthenticationPrincipal LoginUser loginUser);
}
