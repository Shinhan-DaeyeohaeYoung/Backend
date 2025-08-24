package com.joeun.api.item.dto;

public final class UnitPhotoDtos {

    // 생성/교체 요청
    public record UpsertRequest(
            Long id,
            String key,        // S3 key (필수)
            String mime,       // image/jpeg 등 (옵션)
            String hash,       // sha256... (옵션)
            String takenAt    // ISO-8601, null이면 now
    ) {}

    // 조회 응답
    public record DetailResponse(
            Long id,
            String key,
            String mime,
            String hash,
            String takenAt
    ) {}
}
