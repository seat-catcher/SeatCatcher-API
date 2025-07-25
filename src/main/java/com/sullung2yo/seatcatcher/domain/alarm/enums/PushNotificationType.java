package com.sullung2yo.seatcatcher.domain.alarm.enums;

public enum PushNotificationType {
    HELLO(
      "ACK",
      "Hello, FCM!"
    ),
    MOST_USED_TIME_ALERT(
            "지하철에 타셨나요?",
            "앉은 좌석 입력하고 뽑기 한 판 해볼까요?"
    ),
    ARRIVAL_HANDLED(
            "하차 처리",
            "탑승하신 열차가 하차역에 도착하여 자동 하차 처리되었어요."
    ),
    SEAT_REQUEST_RECEIVED(
            "%s님의 자리 요청",
            "좌석 요청이 도착했어요. %s님에게 넘기고 %d 크레딧을 획득해요."
    ),
    SEAT_REQUEST_ACCEPTED(
            "좌석 요청 수락",
            "%s님이 좌석 요청을 수락하였어요. %s역에 도착한 후 자동 하차됩니다."
    ),
    SEAT_REQUEST_REJECTED(
            "좌석 요청 거절",
            "좌석 요청이 거절됐어요."
    ),
    SEAT_REQUEST_ACCEPTED_ARRIVA(
            "앞자리에 도달",
            "%s님과 자리를 교환해주세요."
    ),
    SEAT_EXCHANGE_SUCCESS(
    "%d 크레딧 획득",
            "%s님에게 자리를 양보하여 %d 크레딧을 획득했어요!"
    ),
    SEAT_REQUEST_CANCELED(
            "좌석 요청 취소",
            "%s님이 좌석 요청을 취소하였어요."
    );

    private final String titleTemplate;
    private final String bodyTemplate;

    PushNotificationType(String titleTemplate, String bodyTemplate) {
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public String generateTitle(Object... args) {
        return String.format(titleTemplate, args);
    }

    public String generateBody(Object... args) {
        return String.format(bodyTemplate, args);
    }
}