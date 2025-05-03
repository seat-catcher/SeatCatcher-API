package com.sullung2yo.seatcatcher.user.domain;

public enum PushNotificationType {
    MOST_USED_TIME_ALERT("매일 가장 많이 이용한 시간대 알림"),
    ARRIVAL_HANDLED("하차 처리 완료"),
    SEAT_REQUEST_ACCEPTED("좌석 요청 수락"),
    SEAT_REQUEST_RECEIVED("좌석 요청 도착"),
    SEAT_REQUEST_REJECTED("좌석 요청 거절"),
    SEAT_REQUEST_ACCEPTED_ARRIVA("수락된 요청 - 전역 도달");

    private final String description;

    PushNotificationType(String description) {
        this.description = description;
    }
}
