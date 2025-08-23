package com.joeun.api.item.controller;

import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.ItemDtos.*;
import com.joeun.api.item.dto.AdminItemRegisterDtos.*;
import com.joeun.api.item.service.ItemApplicationService;
import com.joeun.api.item.service.AdminItemOrchestrator;
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

    /** 관리자 리스트: Page -> PageResponse로 감싸 깔끔한 JSON 반환 */
    @GetMapping
    public PageResponse<ItemDtos.ItemSummaryResponse> adminList(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(app.listForUser(pageable));
    }

    /** 아이템 생성 */
    @PostMapping
    public Map<String, Long> create(@RequestBody ItemCreateRequest req) {
        return Map.of("id", app.createItem(req));
    }

    /** 아이템 수정 */
    @PatchMapping("/{itemId}")
    public Map<String, Object> patch(@PathVariable Long itemId, @RequestBody ItemPatchRequest req) {
        app.patchItem(itemId, req);
        return Map.of("id", itemId, "patched", true);
    }

    /** 관리자 상세: 기존 app.adminDetail 포맷 유지 (item + units 페이지 요약) */
    @GetMapping("/{itemId}")
    public Map<String, Object> adminDetail(@PathVariable Long itemId,
                                           @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return app.adminDetail(itemId, pageable);
    }

    /** 유닛 일괄 등록 */
    @PostMapping("/{itemId}/units")
    public Map<String, Object> createUnits(@PathVariable Long itemId,
                                           @RequestBody UnitBatchCreateRequest req) {
        return app.createUnits(itemId, req);
    }

    /** (선택) 아이템 + 유닛 한번에 등록 */
    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest req) {
        return orchestrator.registerWithUnits(req);
    }
}
