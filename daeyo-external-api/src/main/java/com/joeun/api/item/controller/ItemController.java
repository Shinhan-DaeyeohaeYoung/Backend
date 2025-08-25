package com.joeun.api.item.controller;

import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.item.service.ItemApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ItemDtos.ItemDetailResponse userDetail(@PathVariable Long itemId,
                                                  @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return app.getItemDetail(itemId, pageable, true, true);
    }

    /** 유닛 단건 사진 조회 */
    @GetMapping("/items/{itemId}/units/{assetNo}/photo")
    public UnitPhotoDtos.DetailResponse unitPhoto(@PathVariable Long itemId,
                                                  @PathVariable String assetNo) {
        return app.getUnitPhoto(itemId, assetNo);
    }
}
