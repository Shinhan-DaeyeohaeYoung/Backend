package com.joeun.api.waitlist.controller;

import com.joeun.api.waitlist.api.WaitlistApi;
import com.joeun.api.waitlist.service.WaitlistService;
import com.joeun.global.config.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/waitlists")
public class WaitlistController implements WaitlistApi {

    private final WaitlistService waitlistService;

    @PostMapping("/items/{itemId}")
    public ResponseEntity<Void> joinWaitListByItemId(@PathVariable @Valid Long itemId,
                                                     @AuthenticationPrincipal LoginUser loginUser) {
        waitlistService.joinWaitListByItemId(itemId, loginUser);
        return ResponseEntity.ok().build();
    }
}
