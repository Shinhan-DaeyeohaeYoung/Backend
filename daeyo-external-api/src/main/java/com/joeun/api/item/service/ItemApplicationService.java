package com.joeun.api.item.service;

import com.joeun.api.item.dto.AdminItemRegisterDtos;
import com.joeun.api.item.dto.AdminItemRegisterDtos.UnitBatchCreateRequest;
import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.item.dto.ItemDtos.*;
import com.joeun.api.item.dto.UnitPhotoDtos;
import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.service.ItemDomainService;
import com.joeun.domain.item.service.UnitPhotoDomainService;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.global.config.LoginUser;
import com.joeun.service.organization.OrganizationDomainService;
import com.joeun.service.rental.RentalDomainService;
import com.joeun.service.waitlist.WaitlistDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ItemApplicationService {

    private final ItemDomainService itemDomainService;
    private final UnitPhotoDomainService unitPhotoDomainService;
    private final RentalDomainService rentalDomainService;
    private final OrganizationService organizationService;
    private final WaitlistDomainService waitlistDomainService;

    /* ===== 공통 헬퍼 ===== */

    private static boolean isAdminRole(String role) {
        return role != null && (role.equals("ORG_ADMIN") || role.equals("ADMIN"));
    }

    /** 내 멤버십을 orgId -> 응답 DTO로 매핑 */
    private Map<Long, MyOrganizationResponse> myOrgMap(LoginUser loginUser) {
        return organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .collect(Collectors.toMap(
                        MyOrganizationResponse::getOrganizationId,
                        m -> m,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /** 역할 기준으로 orgId 집합 취득 (adminOnly=true면 관리자 멤버십만) */
    private Set<Long> orgIdsByRole(LoginUser loginUser, boolean adminOnly) {
        return organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .filter(m -> !adminOnly || isAdminRole(m.getRole()))
                .map(MyOrganizationResponse::getOrganizationId)
                .collect(Collectors.toSet());
    }

    /** (adminOnly 기준으로) 접근 가능한 org/univ 조합에서 itemId를 찾아 로드 */
    private com.joeun.domain.item.entity.Item loadItemAccessible(LoginUser loginUser, Long itemId, boolean adminOnly) {
        var map = myOrgMap(loginUser).values().stream()
                .filter(m -> !adminOnly || isAdminRole(m.getRole()))
                .toList();

        for (var m : map) {
            try {
                return itemDomainService.getByTenant(m.getUniversityId(), m.getOrganizationId(), itemId);
            } catch (NoSuchElementException ignore) {
                // 다음 멤버십으로 시도
            }
        }
        throw new NoSuchElementException("item not found or no accessible membership for the item");
    }

    private void assertAdminForOrg(LoginUser loginUser, Long orgId) {
        var m = myOrgMap(loginUser).get(orgId);
        if (m == null || !isAdminRole(m.getRole())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "organization admin only"
            );
        }
    }

    /* ===== 목록 ===== */

    /** 일반 사용자: 내가 속한 모든 org 기준 */
    public Page<ItemDtos.ItemSummaryResponse> listForUser(LoginUser loginUser, Pageable pageable) {
        Set<Long> orgIds = orgIdsByRole(loginUser, false); // 모든 멤버십
        if (orgIds.isEmpty()) return Page.empty(pageable);

        return itemDomainService.listActive(loginUser.id(), orgIds, pageable)
                .map(i -> {
                    var cover = unitPhotoDomainService
                            .findItemCover(i.getUniversityId(), i.getOrganizationId(), i.getId())
                            .orElse(null);
                    return new ItemDtos.ItemSummaryResponse(
                            i.getId(),
                            i.getUniversityId(),
                            i.getOrganizationId(),
                            i.getName(),
                            i.getDescription(),
                            i.getTotalQuantity(),
                            i.getAvailableQuantity(),
                            waitlistDomainService.getWaitListCount(i.getId()),
                            i.getIsActive(),
                            cover == null ? null : cover.getImageKey()
                    );
                });
    }

    /** 관리자: 관리자 역할인 org만 */
    public Page<ItemDtos.ItemSummaryResponse> listForAdmin(LoginUser loginUser, Pageable pageable) {
        Set<Long> orgIds = orgIdsByRole(loginUser, true); // 관리자 멤버십만
        if (orgIds.isEmpty()) return Page.empty(pageable);

        return itemDomainService.listActive(loginUser.id(), orgIds, pageable)
                .map(i -> {
                    var cover = unitPhotoDomainService
                            .findItemCover(i.getUniversityId(), i.getOrganizationId(), i.getId())
                            .orElse(null);
                    return new ItemDtos.ItemSummaryResponse(
                            i.getId(),
                            i.getUniversityId(),
                            i.getOrganizationId(),
                            i.getName(),
                            i.getDescription(),
                            i.getTotalQuantity(),
                            i.getAvailableQuantity(),
                            waitlistDomainService.getWaitListCount(i.getId()),
                            i.getIsActive(),
                            cover == null ? null : cover.getImageKey()
                    );
                });
    }

    /* ===== 상세 ===== */

    /** 사용자 상세: 내 모든 멤버십(org) 중 소속된 곳에서 탐색 */
    public ItemDtos.ItemDetailResponse getItemDetailForUser(Long itemId, Pageable pageable,
                                                            boolean includeUnits, boolean includeRentalBrief,
                                                            LoginUser loginUser) {
        return buildDetail(loadItemAccessible(loginUser, itemId, false), pageable, includeUnits, includeRentalBrief);
    }

    /** 관리자 상세: 관리자 멤버십(org)에서만 탐색 */
    public ItemDtos.ItemDetailResponse getItemDetailForAdmin(Long itemId, Pageable pageable,
                                                             boolean includeUnits, boolean includeRentalBrief,
                                                             LoginUser loginUser) {
        return buildDetail(loadItemAccessible(loginUser, itemId, true), pageable, includeUnits, includeRentalBrief);
    }

    private ItemDtos.ItemDetailResponse buildDetail(com.joeun.domain.item.entity.Item item,
                                                    Pageable pageable,
                                                    boolean includeUnits, boolean includeRentalBrief) {
        Long u = item.getUniversityId(), o = item.getOrganizationId();
        Long itemId = item.getId();

        var stats = itemDomainService.unitStats(u, o, itemId);

        var photos = unitPhotoDomainService.listItemUnitPhotos(u, o, itemId).stream()
                .map(p -> new ItemDtos.UnitPhotoSummary(p.getUnit().getAssetNo(), p.getImageKey()))
                .toList();

        ItemDtos.UnitPageResponse unitsDto = null;
        if (includeUnits) {
            Page<IndividualItem> unitsPage = itemDomainService.listUnits(u, o, itemId, pageable);

            Map<Long, ItemDtos.RentalBrief> briefMap = Map.of();
            if (includeRentalBrief && !unitsPage.isEmpty()) {
                var unitIds = unitsPage.getContent().stream().map(IndividualItem::getId).toList();
                List<Rental> rentals = rentalDomainService.findActiveByUnitIds(u, unitIds);
                briefMap = rentals.stream().collect(Collectors.toMap(
                        r -> r.getUnit().getId(),
                        r -> new ItemDtos.RentalBrief(r.getId(), r.getUserId(), r.getDueAt()),
                        (a, b) -> a
                ));
            }

            final Map<Long, ItemDtos.RentalBrief> finalBriefMap = briefMap;
            var content = unitsPage.getContent().stream()
                    .map(u0 -> new ItemDtos.UnitPageResponse.UnitSummary(
                            u0.getId(), itemId, u0.getStatus().name(), u0.getAssetNo(), finalBriefMap.get(u0.getId())
                    ))
                    .toList();

            unitsDto = new ItemDtos.UnitPageResponse(
                    content, unitsPage.getNumber(), unitsPage.getSize(), unitsPage.getTotalElements()
            );
        }

        return new ItemDtos.ItemDetailResponse(
                item.getId(), item.getUniversityId(), item.getOrganizationId(),
                item.getName(), item.getDescription(), item.getDeposit(), item.getMaxRentalDays(),
                item.getTotalQuantity(), item.getAvailableQuantity(), waitlistDomainService.getWaitListCount(itemId) ,item.getIsActive(),
                stats, photos, unitsDto
        );
    }

    /* ===== 사진 (관리자 전용으로 쓰면 adminOnly 강제) ===== */

    public Long upsertUnitPhoto(Long itemId, String assetNo, UnitPhotoDtos.UpsertRequest req, LoginUser loginUser) {
        var item = loadItemAccessible(loginUser, itemId, true); // 관리자만
        Long u = item.getUniversityId(), o = item.getOrganizationId();
        LocalDateTime takenAt = (req.takenAt() == null ? LocalDateTime.now() : LocalDateTime.parse(req.takenAt()));
        return unitPhotoDomainService.upsertUnitPhotoByAssetNo(u, o, itemId, assetNo,
                req.key(), req.mime(), req.hash(), takenAt);
    }

    public void deleteUnitPhoto(Long itemId, String assetNo, LoginUser loginUser) {
        var item = loadItemAccessible(loginUser, itemId, true); // 관리자만
        unitPhotoDomainService.deleteUnitPhotoByAssetNo(
                item.getUniversityId(), item.getOrganizationId(), itemId, assetNo
        );
    }

    /* ===== 관리자: 생성/수정/유닛등록 (관리자 멤버십만 허용) ===== */

    public Long createItem(AdminItemRegisterDtos.ItemCreateRequest req, LoginUser loginUser) {
        // orgId 우선순위: 요청값 → (내 관리자 멤버십이 1개면 그거) → 모호/없음이면 에러
        var adminOrgMap = organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .filter(m -> isAdminRole(m.getRole()))
                .collect(Collectors.toMap(
                        MyOrganizationResponse::getOrganizationId, m -> m,
                        (a, b) -> a, LinkedHashMap::new
                ));

        Long o = (req.organizationId() != null)
                ? req.organizationId()
                : (adminOrgMap.size() == 1 ? adminOrgMap.keySet().iterator().next() : null);

        if (o == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "organizationId is required (multiple or no admin memberships)"
            );
        }
        var m = adminOrgMap.get(o);
        if (m == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "organization admin only"
            );
        }

        Long u = (req.universityId() != null) ? req.universityId() : m.getUniversityId();

        return itemDomainService.createItem(
                u, o, req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive()
        ).getId();
    }

    public void patchItem(Long itemId, AdminItemRegisterDtos.ItemPatchRequest req, LoginUser loginUser) {
        var item = loadItemAccessible(loginUser, itemId, true); // 관리자만
        itemDomainService.patchItem(
                item.getUniversityId(), item.getOrganizationId(), itemId,
                req.name(), req.description(), req.deposit(), req.maxRentalDays(), req.isActive()
        );
    }

    public Map<String, Object> createUnits(Long itemId, UnitBatchCreateRequest req, LoginUser loginUser) {
        var item = loadItemAccessible(loginUser, itemId, true); // 관리자만
        Long u = item.getUniversityId(), o = item.getOrganizationId();

        var createdAssetNos = itemDomainService.createUnits(
                u, o, itemId,
                req.units().stream()
                        .map(x -> new com.joeun.domain.item.service.ItemDomainService.UnitCreate(
                                x.assetNo(), x.description(), x.status()))
                        .toList()
        );

        for (var x : req.units()) {
            var p = x.photo();
            if (p != null && StringUtils.hasText(p.key())) {
                unitPhotoDomainService.upsertUnitPhotoByAssetNo(
                        u, o, itemId, x.assetNo(), p.key(), p.mime(), p.hash(), null);
            }
        }
        return Map.of("created", createdAssetNos.size(), "assetNos", createdAssetNos);
    }

    /** (필요 시) 사용자용 유닛 사진 조회 */
    public UnitPhotoDtos.DetailResponse getUnitPhoto(Long itemId, String assetNo, LoginUser loginUser) {
        var item = loadItemAccessible(loginUser, itemId, false); // 사용자: 모든 멤버십 허용
        var p = unitPhotoDomainService.getUnitPhotoByAssetNo(
                item.getUniversityId(), item.getOrganizationId(), itemId, assetNo
        );
        return new UnitPhotoDtos.DetailResponse(
                p.getId(), p.getImageKey(), p.getMime(), p.getHash(),
                p.getTakenAt() == null ? null : p.getTakenAt().toString()
        );
    }
    public int deleteItemWithUnits(Long itemId, Long organizationId, LoginUser loginUser) {
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        var m = organizationService.getMyOrganization(loginUser, organizationId);
        // 권한 체크 (관리자만)
        var role = m.getRole();
        if (role == null || !(role.equals("ORG_ADMIN") || role.equals("ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 조직의 관리자만 삭제할 수 있습니다.");
        }

        Long u = m.getUniversityId();
        Long o = m.getOrganizationId();

        return itemDomainService.deleteItemCascade(u, o, itemId);
    }

    public void deleteUnit(Long itemId, String assetNo, Long organizationId, LoginUser loginUser) {
        if (loginUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        var m = organizationService.getMyOrganization(loginUser, organizationId);
        var role = m.getRole();
        if (role == null || !(role.equals("ORG_ADMIN") || role.equals("ADMIN"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 조직의 관리자만 유닛을 삭제할 수 있습니다.");
        }

        Long u = m.getUniversityId();
        Long o = m.getOrganizationId();

        itemDomainService.deleteUnitByAssetNo(u, o, itemId, assetNo);
    }

}
