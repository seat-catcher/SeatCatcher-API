package com.sullung2yo.seatcatcher.user.dto.response;

import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.PushNotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "유저 알람  DTO")
public class UserAlarmResponse {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class UserAlarmScrollResponse {
        private List<UserAlarmResponse.UserAlarmItem> userAlarmItemList;
        private Long nextCursor; // 다음 커서 값
        boolean isLast;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserAlarmItem{
        @Schema(description = "알림 아이디")
        private Long id;

        @Schema(description = "알람 타입")
        private PushNotificationType type;

        @Schema(description = "읽음 여부")
        private boolean isRead;

        @Schema(description = "알람 내용")
        private String content;

        @Schema(description = "시간")
        private LocalDateTime localDateTime;

    }
}
