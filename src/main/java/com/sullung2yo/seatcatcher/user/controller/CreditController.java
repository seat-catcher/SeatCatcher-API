//package com.sullung2yo.seatcatcher.user.controller;
//
//import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
//import com.sullung2yo.seatcatcher.common.exception.TokenException;
//import com.sullung2yo.seatcatcher.user.domain.User;
//import com.sullung2yo.seatcatcher.user.domain.UserTagType;
//import com.sullung2yo.seatcatcher.user.dto.response.UserInformationResponse;
//import com.sullung2yo.seatcatcher.user.service.UserService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.constraints.Min;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/credit")
//@Tag(name = "Credit API", description = "Credit APIs")
//public class CreditController {
//
//    private final UserService userService;
//
//    @PatchMapping("/increase")
//    @Operation(
//            summary = "사용자 크레딧 증가 API",
//            description = "사용자의 크레딧을 직접 증가시키는 Endpoint입니다. 내부적으로 PATCH :: /user/me 와 동일한 동작을 수행합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "유저의 총 크레딧 업데이트 완료"
//                    )
//            }
//    )
//    public ResponseEntity<UserInformationResponse> increaseCredit(
//            @RequestHeader("Authorization") String bearerToken,
//            @RequestParam @Min(value = 1, message = "증가량은 1 이상이어야 합니다.") long amount
//    )
//    {
//        // Bearer 토큰 검증
//        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
//            log.error("올바른 JWT 형식이 아닙니다.");
//            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
//        }
//        String token = bearerToken.replace("Bearer ", "");
//        log.debug("JWT 파싱 성공");
//
//        User user = userService.increaseCredit(token, amount);
//
//        return getUserInformationResponseResponseEntity(user);
//    }
//
//    @PatchMapping("/decrease")
//    @Operation(
//            summary = "사용자 크레딧 감소 API",
//            description = "사용자의 크레딧을 직접 감소시키는 Endpoint입니다. 내부적으로 PATCH :: /user/me 와 동일한 동작을 수행합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "유저의 총 크레딧 업데이트 완료"
//                    ),
//                    @ApiResponse(
//                            responseCode = "400",
//                            description = "Bad Request - 잔액이 부족하여 실패했음."
//                    )
//            }
//    )
//    public ResponseEntity<UserInformationResponse> decreaseCredit(
//            @RequestHeader("Authorization") String bearerToken,
//            @RequestParam @Min(value = 1, message = "감소량은 1 이상이어야 합니다.") long amount
//    )
//    {
//        // Bearer 토큰 검증
//        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
//            log.error("올바른 JWT 형식이 아닙니다.");
//            throw new TokenException("올바른 JWT 형식이 아닙니다.", ErrorCode.INVALID_TOKEN);
//        }
//        String token = bearerToken.replace("Bearer ", "");
//        log.debug("JWT 파싱 성공");
//
//        // 잔액 부족으로 실패할 경우 Global Exception Handler 에 의해 처리됨.
//        User user = userService.decreaseCredit(token, amount);
//
//        return getUserInformationResponseResponseEntity(user);
//    }
//
//    private ResponseEntity<UserInformationResponse> getUserInformationResponseResponseEntity(User user) {
//        List<UserTagType> tags = user.getUserTag().stream()
//                .map(userTag -> userTag.getTag().getTagName())
//                .toList();
//
//        log.debug("사용자 정보: {}, {}, {}, {}", user.getName(), user.getCredit(), user.getProfileImageNum(), tags);
//        return ResponseEntity.status(HttpStatus.OK).body(
//                UserInformationResponse.builder()
//                        .name(user.getName())
//                        .profileImageNum(user.getProfileImageNum())
//                        .credit(user.getCredit())
//                        .tags(tags)
//                        .hasOnBoarded(user.getHasOnBoarded())
//                        .build()
//        );
//    }
//}
