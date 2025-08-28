package com.joeun.api.item.controller;

import com.joeun.api.item.dto.AdminItemRegisterDtos;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.item.service.AdminItemOrchestrator;
import com.joeun.api.item.service.ItemApplicationService;
import com.joeun.global.config.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/items")
@SecurityRequirement(name = "Authorization")
@Tag(name = "Admin Items", description = "관리자용 아이템/유닛 관리 API")
public class AdminItemController {

    private final ItemApplicationService app;
    private final AdminItemOrchestrator orchestrator;

//    /** 유닛 사진 업서트(등록/교체) */
//    @Operation(
//            summary = "유닛 사진 업서트(등록/교체)",
//            description = "특정 아이템의 개별 유닛(assetNo)에 대해 사진 메타(키/해시/촬영시각 등)를 등록하거나 교체합니다."
//    )
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "업서트 성공",
//                    content = @Content(mediaType = "application/json",
//                            examples = @ExampleObject(value = "{\"id\":123,\"replaced\":true}"))),
//            @ApiResponse(responseCode = "404", description = "아이템/유닛/테넌트 불일치")
//    })
//    @PageableAsQueryParam
//    @PostMapping("/{itemId}/units/{assetNo}/photo")
//    public Map<String, Object> upsertUnitPhoto(@PathVariable Long itemId,
//                                               @AuthenticationPrincipal LoginUser loginUser,
//                                               @PathVariable String assetNo,
//                                               @RequestBody UnitPhotoDtos.UpsertRequest req) {
//        Long photoId = app.upsertUnitPhoto(itemId, assetNo, req,loginUser);
//        return Map.of("id", photoId, "replaced", true);
//    }

    /** 관리자 리스트 */
    @Operation(
            summary = "아이템 목록(관리자)",
            description = "관리자 관점의 아이템 페이지 목록을 조회합니다. 썸네일(커버) 키 등 요약 정보 포함."
    )
    @Parameters({
            @Parameter(name = "page", description = "0부터 시작 페이지", example = "0"),
            @Parameter(name = "size", description = "페이지 크기", example = "20"),
            @Parameter(name = "sort", description = "정렬 (예: id,asc | name,desc)", example = "id,asc")
    })
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = PageResponse.class)))
    })
    @PageableAsQueryParam
    @GetMapping
    public PageResponse<ItemDtos.ItemSummaryResponse> adminList(
            @Parameter(hidden = true)
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(app.listForUser(loginUser,pageable));
    }

    /** 아이템 생성 (Admin DTO 사용) */
    @Operation(
            summary = "아이템 생성",
            description = "관리자 전용 아이템 생성. 필드: 이름/설명/보증금/최대대여일/활성여부 등.(userid, organizationid 미입력시 현재 로그인한 사용자로 들어감)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1}")))
    })
    @PageableAsQueryParam
    @PostMapping
    public Map<String, Long> create(@RequestBody AdminItemRegisterDtos.ItemCreateRequest req,
                                    @AuthenticationPrincipal LoginUser loginUser
                                    ) {
        return Map.of("id", app.createItem(req,loginUser));
    }

    /** 아이템 수정 (Admin DTO 사용) */
    @Operation(
            summary = "아이템 수정",
            description = "관리자 전용 아이템 부분 수정(PATCH). null이 아닌 필드만 반영."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\": 1, \"patched\": true}"))),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    @PageableAsQueryParam
    @PatchMapping("/{itemId}")
    public Map<String, Object> patch(@PathVariable Long itemId,
                                     @RequestBody AdminItemRegisterDtos.ItemPatchRequest req,
                                     @AuthenticationPrincipal LoginUser loginUser
                                     ) {
        app.patchItem(itemId, req,loginUser);
        return Map.of("id", itemId, "patched", true);
    }

    /** 관리자 상세 */
    @Operation(
            summary = "아이템 상세(관리자)",
            description = "관리자 관점 상세. 아이템 기본정보 + 유닛 페이지 목록(요약) 구성으로 반환."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = ItemDtos.ItemDetailResponse.class)))
    })
    @PageableAsQueryParam
    @GetMapping("/{itemId}")
    public ItemDtos.ItemDetailResponse adminDetail(@PathVariable Long itemId,
                                                   @Parameter(hidden = true)
                                                   @AuthenticationPrincipal LoginUser loginUser,
                                                   @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return app.getItemDetailForAdmin(itemId, pageable, true, true,loginUser);
    }


    /** 유닛 일괄 등록 (Admin DTO 사용) */
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "{ \"created\": 2, \"assetNos\": [\"501\",\"502\"] }"
                            )))
    })
    @PostMapping("/{itemId}/units")
    public Map<String, Object> createUnits(
            @PathVariable Long itemId,
            @AuthenticationPrincipal LoginUser loginUser,
            @org.springframework.web.bind.annotation.RequestBody AdminItemRegisterDtos.UnitBatchCreateRequest req
    ) {
        return app.createUnits(itemId, req,loginUser);
    }

    /** (선택) 아이템 + 유닛 한번에 등록 */
    @Operation(
            summary = "아이템+유닛 한번에 등록",
            description = "아이템을 생성하면서 유닛까지 함께 등록하는 오케스트레이션 엔드포인트."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = AdminItemRegisterDtos.RegisterResponse.class)))
    })
    @PageableAsQueryParam
    @PostMapping("/register")
    public AdminItemRegisterDtos.RegisterResponse register(@Parameter(hidden = true)
                                                               @RequestBody AdminItemRegisterDtos.RegisterRequest req) {
        return orchestrator.registerWithUnits(req);
    }

    @DeleteMapping("/{itemId}")
    public Map<String, Object> deleteItem(@PathVariable Long itemId,
                                          @RequestParam Long organizationId,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        int unitsDeleted = app.deleteItemWithUnits(itemId, organizationId, loginUser);
        return Map.of("id", itemId, "deleted", true, "unitsDeleted", unitsDeleted);
    }

    @DeleteMapping("/{itemId}/units/{assetNo}")
    public Map<String, Object> deleteUnit(@PathVariable Long itemId,
                                          @PathVariable String assetNo,
                                          @RequestParam Long organizationId,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        app.deleteUnit(itemId, assetNo, organizationId, loginUser);
        return Map.of("itemId", itemId, "assetNo", assetNo, "deleted", true);
    }



}
