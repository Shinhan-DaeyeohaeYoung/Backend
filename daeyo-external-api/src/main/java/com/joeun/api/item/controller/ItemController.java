package com.joeun.api.item.controller;

import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.PageResponse;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.item.service.ItemApplicationService;
import com.joeun.global.config.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "Items", description = "사용자용 아이템 조회 API")
public class ItemController {

    private final ItemApplicationService app;
    @Operation(
            summary = "아이템 목록",
            parameters = {
                    @Parameter(name = "page", example = "0"),
                    @Parameter(name = "size", example = "20"),
                    @Parameter(name = "sort", example = "id,desc")
            }
    )
    @PageableAsQueryParam
    @GetMapping("/items")
    public PageResponse<ItemDtos.ItemSummaryResponse> list(
            @Parameter(hidden = true)
            @AuthenticationPrincipal LoginUser loginUser,
            @Parameter(hidden = true)
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable){
        System.out.println(loginUser.id());
        return PageResponse.from(app.listForUser(loginUser, pageable));
    }
    @Operation(
            summary = "아이템 상세(사용자)",
            description = "아이템 기본정보와 상태 통계(unitStats), 각 개별자산의 대표 사진 키 목록을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "상세 조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ItemDtos.ItemDetailResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "id": 1,
                      "universityId": 1,
                      "organizationId": 2,
                      "name": "충전기",
                      "description": "C타입 65W 충전기",
                      "deposit": 10000,
                      "maxRentalDays": 7,
                      "totalQuantity": 2,
                      "availableQuantity": 2,
                      "isActive": true,
                      "unitStats": {
                        "AVAILABLE": 0,
                        "RESERVED": 0,
                        "RENTED": 2,
                        "REPAIR": 0,
                        "LOST": 0,
                        "DISPOSED": 0
                      },
                      "photos": [
                        { "assetNo": "501", "key": "univ/1/items/1/units/501.jpg" },
                        { "assetNo": "502", "key": "univ/1/items/1/units/502.jpg" }
                      ]
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "아이템 없음")
    })
    @PageableAsQueryParam
    @GetMapping("/items/{itemId}")
    public ItemDtos.ItemDetailResponse userDetail(
            @PathVariable Long itemId,
            @Parameter(hidden = true)
            @PageableDefault(size = 50, sort = "id") Pageable pageable,
            @AuthenticationPrincipal LoginUser loginUser) {
        return app.getItemDetailForUser(itemId, pageable, true, true,loginUser);
    }

    /** 유닛 단건 사진 조회 */
    @Operation(
            summary = "유닛 단건 사진 조회",
            description = "특정 아이템의 개별자산(assetNo)에 매핑된 사진 메타 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = UnitPhotoDtos.DetailResponse.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                      "id": 123,
                      "imageKey": "univ/1/items/1/units/501.jpg",
                      "mime": "image/jpeg",
                      "hash": "3f7850...ab",
                      "takenAt": "2025-08-23T21:15:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "사진 없음(해당 assetNo 미등록)")
    })
    @GetMapping("/items/{itemId}/units/{assetNo}/photo")
    public UnitPhotoDtos.DetailResponse unitPhoto(@PathVariable Long itemId,
                                                  @PathVariable String assetNo,
                                                  @AuthenticationPrincipal LoginUser loginUser) {
        return app.getUnitPhoto(itemId, assetNo,loginUser);
    }
}
