package com.joeun.api.item.controller;

import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.service.ItemApplicationService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ItemController {
    private final ItemApplicationService app;

    @GetMapping("/items")
    public PageResponse<ItemDtos.ItemSummaryResponse> list(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(app.listForUser(pageable));
    }

    @GetMapping("/items/{itemId}")
    public ItemDtos.ItemDetailResponse detail(@PathVariable Long itemId) {
        return app.getForUser(itemId);
    }
}
