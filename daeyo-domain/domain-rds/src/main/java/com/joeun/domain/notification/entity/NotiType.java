package com.joeun.domain.notification.entity;

public enum NotiType {
    // 대기열에서 나와 홀딩을 시작하기 위한 알림
    WAITING_LIST_ESCAPE("물품 대여 차례 알림", "대기열에서 나와 홀딩을 시작합니다. 홀딩을 시작하려면 아래 버튼을 눌러주세요."),
    // 제한시간이 만료되어 홀딩이 취소되었음을 알리는 알림
    HOLDING_CANCEL("홀딩 취소 알림", "홀딩이 취소되었습니다. 다시 대기열에 등록해주세요.");

    private final String title;
    private final String message;

    NotiType(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

}
