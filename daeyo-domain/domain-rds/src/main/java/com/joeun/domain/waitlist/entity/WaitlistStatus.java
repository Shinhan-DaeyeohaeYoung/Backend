package com.joeun.domain.waitlist.entity;

public enum WaitlistStatus {
    // 대기중
    WAITING,
    // 제안됨
    OFFERED,
    // 알림 발송됨
    NOTIFIED,
    // 취소됨
    CANCELLED,
    // 대여 완료됨
    FULFILLED,
    // 대기열 무시됨 (에러 플래그)
    SKIPPED,
}
