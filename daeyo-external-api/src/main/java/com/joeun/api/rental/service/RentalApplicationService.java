package com.joeun.api.rental.service;

import com.joeun.api.item.dto.ItemDtos;
import com.joeun.api.organization.dto.MyOrganizationResponse;
import com.joeun.api.organization.service.OrganizationService;
import com.joeun.api.rental.dto.RentalDtos;
import com.joeun.api.rental.dto.RentalDtos.*;
import com.joeun.domain.item.entity.Item;
import com.joeun.domain.item.entity.UnitPhoto;
import com.joeun.domain.item.service.ItemDomainService;
import com.joeun.domain.item.service.UnitPhotoDomainService;
import com.joeun.domain.rental.entity.Rental;
import com.joeun.domain.rental.entity.RentalStatus;
import com.joeun.global.config.LoginUser;
import com.joeun.infra.aws.s3.service.ImageInfraService;
import com.joeun.service.rental.RentalDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalApplicationService {

    private final RentalDomainService domain;
    private final ItemDomainService itemDomainService;          //  itemId → (u,o) 해석용
    private final OrganizationService organizationService;      //  멤버십/권한 확인용
    private final UnitPhotoDomainService unitPhotoDomainService;
    private final ImageInfraService imageInfraService;

    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /* ===================== 공통 헬퍼 ===================== */

    private static boolean isAdminRole(String role) {
        return role != null && (role.equals("ORG_ADMIN") || role.equals("ADMIN"));
    }

    /** 내 모든 멤버십 orgId 집합 */
    private Set<Long> myOrgIds(LoginUser loginUser) {
        return organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .map(MyOrganizationResponse::getOrganizationId)
                .collect(Collectors.toSet());
    }

    /** 해당 org에 대해 내가 관리자임을 보장 (아니면 403) */
    private void assertAdmin(LoginUser loginUser, Long orgId) {
        var m = organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .filter(x -> Objects.equals(x.getOrganizationId(), orgId))
                .findFirst()
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.FORBIDDEN, "No membership for organizationId=" + orgId));

        if (!isAdminRole(m.getRole())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "Organization admin only");
        }
    }

    /** itemId가 속한 (u,o)를 내 멤버십으로 해석 (없으면 404) */
    private Item resolveItemAccessible(LoginUser loginUser, Long itemId) {
        var orgMap = organizationService.getMyOrganizations(loginUser, "")
                .stream()
                .collect(Collectors.toMap(
                        MyOrganizationResponse::getOrganizationId,
                        MyOrganizationResponse::getUniversityId,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        for (var e : orgMap.entrySet()) {
            Long o = e.getKey(), u = e.getValue();
            try {
                return itemDomainService.getByTenant(u, o, itemId);
            } catch (NoSuchElementException ignore) { /* 다른 멤버십으로 시도 */ }
        }
        throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Item not found in your memberships");
    }

    /** rentalId의 (u,o) 가져오기 */
    private UO resolveRentalUO(Long rentalId) {
        Rental r = domain.getById(rentalId);
        if (r == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Rental not found");
        }
        return new UO(r.getUniversityId(), r.getOrganizationId());
    }

    private record UO(Long u, Long o) {}

    /** ✅ unitId 기준 대표 사진 key 조회 */
    private String resolveUnitPhotoKey(Long u, Long o, Long itemId, Long unitId) {
        if (u == null || o == null || itemId == null || unitId == null) return null;
        return unitPhotoDomainService.listItemUnitPhotos(u, o, itemId).stream()
                .filter(p -> p.getUnit() != null && Objects.equals(p.getUnit().getId(), unitId))
                .map(UnitPhoto::getImageKey)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /** ✅ key → presigned URL */
    private String toPresignedUrl(String key) {
        return (key == null || key.isBlank()) ? null : imageInfraService.getDownloadPresignedUrl(key);
    }

    /* ===================== 예약/조회 API ===================== */

    /** 예약: 사용자 멤버십으로 itemId의 (u,o) 해석 후 예약 */
    public ReserveResponse reserve(LoginUser loginUser, ReserveRequest req) {
        Item item = resolveItemAccessible(loginUser, req.itemId());
        Long u = item.getUniversityId(), o = item.getOrganizationId();

        int ttl = (req.ttlMinutes() == null || req.ttlMinutes() <= 0) ? 30 : req.ttlMinutes();
        Long rentalId = domain.reserveUnit(u, o, loginUser.id(), req.itemId(), req.unitId(), ttl);

        return new ReserveResponse(rentalId, req.itemId(), req.unitId(), null, null, "RESERVED");
    }

    /** 내 예약(간단 엔티티) */
    public Page<Rental> myReserved(LoginUser loginUser, Pageable pageable) {
        Long u = loginUser.universityId();
        return domain.listMyActiveReservations(u, loginUser.id(), pageable);
    }

    /** 내 예약(요약 DTO) – ✅ unit 이미지 URL 포함 */
    public Page<RentalDtos.ReservationSummary> listMyReservations(LoginUser loginUser, Pageable pageable) {
        Long u = loginUser.universityId();
        return domain.listMyActiveReservations(u, loginUser.id(), pageable)
                .map(r -> {
                    Long unitId = (r.getUnit() != null ? r.getUnit().getId() : null);
                    Long itemId = (r.getItem() != null ? r.getItem().getId() : null);
                    String key = resolveUnitPhotoKey(r.getUniversityId(), r.getOrganizationId(), itemId, unitId);
                    String url = toPresignedUrl(key);
                    return new RentalDtos.ReservationSummary(
                            r.getId(),
                            (r.getItem() != null ? r.getItem().getId() : null),
                            unitId,
                            r.getStatus().name(),
                            r.getReservedAt() == null ? null : r.getReservedAt().format(F),
                            r.getReserveExpiresAt() == null ? null : r.getReserveExpiresAt().format(F),
                            url
                    );
                });
    }

    public RentalDtos.ApproveResponse approve(LoginUser loginUser, Long rentalId) {
        UO uo = resolveRentalUO(rentalId);

        var approved = domain.approveReservation(uo.u, uo.o, rentalId, loginUser.id());
        return new RentalDtos.ApproveResponse(
                approved.id(),
                approved.status().name(),
                approved.dueAt() == null ? null : approved.dueAt().format(F)
        );
    }

    /** 취소: rentalId의 (u,o) 확인 후 취소 */
    public void cancel(LoginUser loginUser, Long rentalId) {
        UO uo = resolveRentalUO(rentalId);
        domain.cancelReservation(uo.u, uo.o, rentalId, loginUser.id());
    }

    /** 대여 가능 여부 확인 */
    public boolean possible(LoginUser loginUser, Long rentalId) {
        UO uo = resolveRentalUO(rentalId);
        return domain.isRentalPossible(uo.u, uo.o, rentalId, loginUser.id());
    }

    /** 내 대여/예약 히스토리 – ✅ unit 이미지 URL 포함 */
    public Page<RentalDtos.RentalHistoryItem> listMyRentalHistory(
            LoginUser loginUser,
            String statusCsv,
            String fromIso,
            String toIso,
            boolean includeExpiredReservations,
            Pageable pageable
    ) {
        Long u = loginUser.universityId();

        Set<RentalStatus> statuses = parseStatuses(statusCsv);
        LocalDateTime from = parseDateTime(fromIso);
        LocalDateTime to = parseDateTime(toIso);

        Page<Rental> page = domain.listMyHistory(
                u, loginUser.id(), statuses, from, to, includeExpiredReservations, pageable
        );

        final LocalDateTime now = LocalDateTime.now();

        return page.map(r -> {
            boolean expired = r.getStatus() == RentalStatus.RESERVED
                    && r.getReserveExpiresAt() != null
                    && r.getReserveExpiresAt().isBefore(now);

            Long unitId = (r.getUnit() != null ? r.getUnit().getId() : null);
            Long itemId = (r.getItem() != null ? r.getItem().getId() : null);
            String key = resolveUnitPhotoKey(r.getUniversityId(), r.getOrganizationId(), itemId, unitId);
            String url = toPresignedUrl(key);

            return new RentalDtos.RentalHistoryItem(
                    r.getId(),
                    r.getStatus().name(),
                    (r.getItem() != null ? r.getItem().getId() : null),
                    unitId,
                    r.getReservedAt() == null ? null : r.getReservedAt().format(F),
                    r.getReserveExpiresAt() == null ? null : r.getReserveExpiresAt().format(F),
                    r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                    r.getDueAt() == null ? null : r.getDueAt().format(F),
                    r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                    expired,
                    url
            );
        });
    }

    /* ===================== 현재 대여중 ===================== */

    private static final Set<String> ALLOWED_SORTS = Set.of(
            "id", "status", "reservedAt", "reserveExpiresAt",
            "rentedAt", "dueAt", "returnedAt",
            "organizationId", "universityId", "userId"
    );

    private Pageable sanitizeSort(Pageable pageable, String fallbackProperty) {
        if (pageable == null) return PageRequest.of(0, 20, Sort.by(Sort.Order.desc(fallbackProperty)));
        Sort safe = Sort.unsorted();
        for (Sort.Order o : pageable.getSort()) {
            String p = o.getProperty();
            if (ALLOWED_SORTS.contains(p)) {
                safe = safe.and(Sort.by(new Sort.Order(o.getDirection(), p)));
            }
        }
        if (safe.isUnsorted()) safe = Sort.by(Sort.Order.desc(fallbackProperty));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safe);
    }

    /** 내 대여중 전체 – ✅ unit 이미지 URL 포함 */
    public Page<RentalDtos.CurrentRentalItem> listMyCurrentRentals(LoginUser loginUser, Pageable pageable) {
        Long u = loginUser.universityId();
        Pageable safe = sanitizeSort(pageable, "rentedAt");

        Page<Rental> page = domain.listMyCurrentRentals(u, loginUser.id(), safe);
        return page.map(r -> {
            Long unitId = (r.getUnit() != null ? r.getUnit().getId() : null);
            Long itemId = (r.getItem() != null ? r.getItem().getId() : null);
            String key = resolveUnitPhotoKey(r.getUniversityId(), r.getOrganizationId(), itemId, unitId);
            String url = toPresignedUrl(key);

            return new RentalDtos.CurrentRentalItem(
                    r.getId(),
                    r.getUniversityId(),
                    r.getOrganizationId(),
                    r.getUserId(),
                    (r.getItem() != null ? r.getItem().getId() : null),
                    unitId,
                    r.getQuantity(),
                    r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                    r.getDueAt() == null ? null : r.getDueAt().format(F),
                    r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                    r.getStatus().name(),
                    null,      // depositId 필요시 채우기
                    url
            );
        });
    }

    /** 조직별 내 대여중 – ✅ unit 이미지 URL 포함 */
    public Page<RentalDtos.CurrentRentalItem> listMyCurrentRentalsByOrganization(
            LoginUser loginUser, Long organizationId, Pageable pageable
    ) {
        if (!myOrgIds(loginUser).contains(organizationId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "No membership for organizationId=" + organizationId);
        }

        Long u = loginUser.universityId();
        Pageable safe = sanitizeSort(pageable, "rentedAt");

        Page<Rental> page = domain.listMyCurrentRentalsByOrganization(u, organizationId, loginUser.id(), safe);
        return page.map(r -> {
            Long unitId = (r.getUnit() != null ? r.getUnit().getId() : null);
            Long itemId = (r.getItem() != null ? r.getItem().getId() : null);
            String key = resolveUnitPhotoKey(r.getUniversityId(), r.getOrganizationId(), itemId, unitId);
            String url = toPresignedUrl(key);

            return new RentalDtos.CurrentRentalItem(
                    r.getId(),
                    r.getUniversityId(),
                    r.getOrganizationId(),
                    r.getUserId(),
                    (r.getItem() != null ? r.getItem().getId() : null),
                    unitId,
                    r.getQuantity(),
                    r.getRentedAt() == null ? null : r.getRentedAt().format(F),
                    r.getDueAt() == null ? null : r.getDueAt().format(F),
                    r.getReturnedAt() == null ? null : r.getReturnedAt().format(F),
                    r.getStatus().name(),
                    null,
                    url   // ✅ unitImageUrl
            );
        });
    }

    // UnitReservationDetail: 이미 photos 리스트에 URL 포함
    private List<ItemDtos.UnitPhotoSummary> toPhotoSummaries(List<UnitPhoto> photos) {
        return photos.stream()
                .map(p -> {
                    String key = p.getImageKey();
                    String url = (key == null) ? null : imageInfraService.getDownloadPresignedUrl(key);
                    return new ItemDtos.UnitPhotoSummary(
                            p.getUnit() != null ? p.getUnit().getAssetNo() : null, // assetNo
                            key,                                                    // key
                            url                                                     // imageUrl
                    );
                })
                .toList();
    }

    public List<RentalDtos.UnitReservationDetail> listMyReservationsByOrganization(
            LoginUser loginUser, Long organizationId, Pageable pageable
    ) {
        Long u = loginUser.universityId();
        Long userId = loginUser.id();

        List<Rental> rentals = domain.listMyActiveReservationsByOrganizationRaw(u, organizationId, userId);

        List<RentalDtos.UnitReservationDetail> result = new ArrayList<>(rentals.size());
        for (Rental r : rentals) {
            if (r.getUnit() == null) continue;

            var unit   = r.getUnit();
            var unitId = unit.getId();
            var item   = r.getItem();
            var itemId = (r.getItem() != null ? r.getItem().getId() : null);
            var itemDescription = (item != null ? item.getDescription() : null);

            List<UnitPhoto> photoEntities = (itemId == null)
                    ? List.of()
                    : unitPhotoDomainService.listItemUnitPhotos(
                            r.getUniversityId(), r.getOrganizationId(), itemId
                    ).stream()
                    .filter(p -> p.getUnit() != null && Objects.equals(p.getUnit().getId(), unitId))
                    .toList();

            var photos = toPhotoSummaries(photoEntities); // (assetNo, key, imageUrl)

            result.add(new RentalDtos.UnitReservationDetail(
                    r.getId(),                 // rentalId
                    unitId,                    // unitId
                    unit.getAssetNo(),         // assetNo
                    unit.getStatus().name(),   // unitStatus
                    itemId,                    // itemId
                    itemDescription,
                    r.getUniversityId(),       // universityId
                    r.getOrganizationId(),     // organizationId
                    photos                     // unit photos
            ));
        }
        return result;
    }

    /* ===================== 파싱 헬퍼 ===================== */

    private Set<RentalStatus> parseStatuses(String statusCsv) {
        if (statusCsv == null || statusCsv.isBlank()) return Collections.emptySet();
        return Arrays.stream(statusCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(RentalStatus::valueOf)
                .collect(() -> EnumSet.noneOf(RentalStatus.class), EnumSet::add, EnumSet::addAll);
    }

    private LocalDateTime parseDateTime(String iso) {
        if (iso == null || iso.isBlank()) return null;
        return LocalDateTime.parse(iso, F);
    }
}
