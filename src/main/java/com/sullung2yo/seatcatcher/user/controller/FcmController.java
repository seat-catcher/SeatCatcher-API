package com.sullung2yo.seatcatcher.user.controller;

import com.sullung2yo.seatcatcher.domain.alarm.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.domain.alarm.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/token")
    @Operation(
            summary = "fcm 기기별 toekn 저장 API",
            description = "각 기기마다의 fcm token을 user 칼럼에 저장합니다. 아래의 경우 저장이 필요합니다." +
                    "- 앱이 인스턴스 ID를 삭제한 경우\n" +
                    "- 앱이 새 기기에서 복원된 경우\n" +
                    "- 사용자가 앱을 제거하거나 재설치한 경우\n" +
                    "- 사용자가 앱 데이터를 지운 경우",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "fcmToken 저장 예시",
                                            summary = "기본 요청 예시",
                                            value = "{ \"token\": q3498fhiudhf0a9s}"
                                    )
                            }
                    )
            )
    )
    @ApiResponse(responseCode = "200", description = "token 저장 성공")
    @ApiResponse(responseCode = "404", description = "user를 찾을 수 없습니다.")
    public ResponseEntity<String> saveFcmToken(@RequestBody FcmRequest.Token request) {
        fcmService.saveToken(request);
        return ResponseEntity.ok("token을 저장하였습니다.");
    }
}
