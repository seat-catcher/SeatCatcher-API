package com.sullung2yo.seatcatcher.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USER_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 상태 엔티티를 찾을 수 없습니다."),
    INSUFFICIENT_CREDIT(HttpStatus.BAD_REQUEST, "크레딧이 부족합니다."),
    SUBWAY_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "지하철 노선을 찾을 수 없습니다."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "태그를 찾을 수 없습니다."),
    INVALID_PROFILE_IMAGE_NUM(HttpStatus.BAD_REQUEST, "유효하지 않은 프로필 이미지 번호입니다."),
    SUBWAY_LINE_MISMATCH(HttpStatus.BAD_REQUEST, "출발역과 도착역의 노선이 다릅니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    UNKNOWN_INCOMING_TRAINS_RESPONSE_MESSAGE(HttpStatus.INTERNAL_SERVER_ERROR, "확인되지 않은 Response 값입니다."),
    SUBWAY_NOT_FOUND(HttpStatus.NOT_FOUND, "지하철을 찾을 수 없습니다."),
    SUBWAY_STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "지하철 역을 찾을 수 없습니다."),
    PATH_HISTORY_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 이력에 접근할 수 없습니다."),
    PATH_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "경로 이력을 찾을 수 없습니다."),
    INVALID_REQUEST_URI(HttpStatus.BAD_REQUEST,"잘못된 FCM 요청"),
    FIREBASE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"Firebase 서버 오류"),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 좌석을 찾을 수 없습니다."),
    SEAT_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "해당 좌석은 이미 점유중입니다."),
    USER_NOT_RESERVED(HttpStatus.BAD_REQUEST, "해당 사용자는 좌석을 점유중이지 않습니다."),
    USER_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "이미 다른 좌석을 예약한 사용자입니다."),
    TRAIN_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 열차를 찾을 수 없습니다."),
    ALARM_NOT_FOUND(HttpStatus.NOT_FOUND, "알람을 찾을 수 없습니다."),
    ALARM_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 이력에 접근할 수 없습니다."),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    YIELD_ACCEPT_FAILED(HttpStatus.BAD_REQUEST, "양도 요청 수락에 실패했습니다."),
    AUTH_INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "유효하지 않은 인증 제공자입니다."),
    AUTH_KAKAO_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 서버 오류"),
    AUTH_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "인증 정보 파싱 오류"),
    AUTH_APPLE_CLIENT_SECRET_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Apple Client Secret 생성 오류"),
    AUTH_APPLE_PRIVATE_KEY_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Apple 개인키 로드 오류"),
    INVALID_CREDIT_MODIFICATION(HttpStatus.INTERNAL_SERVER_ERROR, "크레딧 수정 결과가 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
