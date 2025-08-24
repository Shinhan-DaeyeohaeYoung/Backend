package com.joeun.api.item.controller;

import com.joeun.api.item.dto.AdminItemRegisterDtos;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.item.service.AdminItemOrchestrator;
import com.joeun.api.item.service.ItemApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/items")
public class AdminItemController {

    private final ItemApplicationService app;
    private final AdminItemOrchestrator orchestrator;

    /** 유닛 사진 업서트(등록/교체) */
    @PostMapping("/{itemId}/units/{assetNo}/photo")
    public Map<String, Object> upsertUnitPhoto(@PathVariable Long itemId,
                                               @PathVariable String assetNo,
                                               @RequestBody UnitPhotoDtos.UpsertRequest req) {
        Long photoId = app.upsertUnitPhoto(itemId, assetNo, req);
        return Map.of("id", photoId, "replaced", true);
    }

    /** 관리자 리스트 */
    @GetMapping
    public PageResponse<ItemDtos.ItemSummaryResponse> adminList(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(app.listForUser(pageable));
    }

    /** 아이템 생성 (Admin DTO 사용) */
    @PostMapping
    public Map<String, Long> create(@RequestBody AdminItemRegisterDtos.ItemCreateRequest req) {
        return Map.of("id", app.createItem(req));
    }

    /** 아이템 수정 (Admin DTO 사용) */
    @PatchMapping("/{itemId}")
    public Map<String, Object> patch(@PathVariable Long itemId,
                                     @RequestBody AdminItemRegisterDtos.ItemPatchRequest req) {
        app.patchItem(itemId, req);
        return Map.of("id", itemId, "patched", true);
    }

    /** 관리자 상세 */
    @GetMapping("/{itemId}")
    public ItemDtos.ItemDetailResponse adminDetail(@PathVariable Long itemId,
                                                   @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return app.getItemDetail(itemId, pageable, true, true);
    }


    /** 유닛 일괄 등록 (Admin DTO 사용) */
    @PostMapping("/{itemId}/units")
    public Map<String, Object> createUnits(@PathVariable Long itemId,
                                           @RequestBody AdminItemRegisterDtos.UnitBatchCreateRequest req) {
        return app.createUnits(itemId, req);
    }

    /** (선택) 아이템 + 유닛 한번에 등록 */
    @PostMapping("/register")
    public AdminItemRegisterDtos.RegisterResponse register(@RequestBody AdminItemRegisterDtos.RegisterRequest req) {
        return orchestrator.registerWithUnits(req);
    }
}
