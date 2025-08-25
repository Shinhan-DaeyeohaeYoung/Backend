package com.joeun.domain.holding.entity;

public enum HoldingStatus {
    OFFERED, // 홀딩 상태
    FULFILLED, // 대여 확정
    CANCELLED, // 사용자 홀딩 취소
    EXPIRED, // 홀딩 시간 만료
    REVOKED // 관리자 홀딩 철회
}
