package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.user.domain.PushNotificationType;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.response.UserAlarmResponse;
import com.sullung2yo.seatcatcher.user.service.UserAlarmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("alarms")
@RequiredArgsConstructor
@Tag(name = "알람 API", description = "알람관련 API 입니다.")
public class UserAlarmController {

    private final UserAlarmService userAlarmService;
    @GetMapping("/me")
    @Operation(
            summary = "사용자 알람 불러오기 API",
            description = "사용자의 모든 알람을 불러옵니다."

    )
    public ResponseEntity<UserAlarmResponse.UserAlarmScrollResponse> getMyAlarms(
            @RequestHeader("Authorization") String bearerToken,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) PushNotificationType type,
            @RequestParam(required = false) Boolean isRead) {
        String token = bearerToken.replace("Bearer ", "");
        UserAlarmResponse.UserAlarmScrollResponse response = userAlarmService.getMyAlarms(token, size, cursor, type,isRead);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{alarmId}")
    @Operation(
            summary = "특정 알람 불러오기 API",
            description = "사용자의 특정 알람을 불러옵니다."

    )
    public ResponseEntity<UserAlarmResponse.UserAlarmItem> getAlarmById(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Long alarmId){
        String token = bearerToken.replace("Bearer ", "");

        UserAlarmResponse.UserAlarmItem response = userAlarmService.getAlarm(token, alarmId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{alarmId}")
    @Operation(
            summary = "사용자 알람 삭제API",
            description = "사용자 알람을 삭제 합니다."

    )
    public ResponseEntity<?> deleteAlarm(@RequestHeader("Authorization") String bearerToken,
                                         @PathVariable Long alarmId){

        String token = bearerToken.replace("Bearer ", "");
        userAlarmService.deletAlarm(token, alarmId);
        return ResponseEntity.ok("알람이 성공적으로 삭제되었습니다.");
    };


    // GET /alarms
    @GetMapping("/")
    @Operation(
            summary = "모든 알람 삭제API",
            description = "admin 관련 기능입니다 미완성 입니다.."

    )
    public ResponseEntity<List<?>> getAllAlarms(){
        return null;
    }
}
