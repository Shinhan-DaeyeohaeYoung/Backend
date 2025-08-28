package com.joeun.domain.notification.entity;

public enum NotiType {
    // 대기열에서 나와 홀딩을 시작하기 위한 알림
    WAITING_LIST_ESCAPE("물품 대여 차례 알림", "대기열에서 나와 예약을 시작합니다. 예약을 시작하려면 아래 버튼을 눌러주세요."),
    // 제한시간이 만료되어 홀딩이 취소되었음을 알리는 알림
    HOLDING_CANCEL("예약 취소 알림", "예약이 취소되었습니다. 다시 예약해주세요."),
    // 대여 예약이 완료되었고, 30분 후에 대여가 자동으로 취소될 예정임을 알리는 알림
    RENTAL_RESERVATION("대여 예약 완료 알림", "대여 예약이 완료되었습니다. 30분 안에 대여를 완료하지 않으면 예약이 자동으로 취소됩니다.");


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
