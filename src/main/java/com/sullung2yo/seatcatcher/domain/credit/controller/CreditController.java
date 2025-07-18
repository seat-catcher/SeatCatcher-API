package com.sullung2yo.seatcatcher.domain.credit.controller;

import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.domain.credit.dto.request.CreditModificationRequest;
import com.sullung2yo.seatcatcher.domain.credit.service.CreditService;
import com.sullung2yo.seatcatcher.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/credit")
public class CreditController {

    private final CreditService creditService;
    private final UserService userService;

    @Operation(
            summary = "Credit 수정 API",
            description = "사용자의 Credit을 추가하거나 차감하는 API입니다. " +
                    "Credit을 추가하려면 isAddition=true로 설정하고, 차감하려면 false로 설정합니다.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Credit 수정 성공"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청"
                    )
            }
    )
    @PatchMapping
    public ResponseEntity<?> creditModification(
            @RequestBody @Valid CreditModificationRequest request,
            @RequestParam("isAddition") boolean isAddition
    ) {
        // Parse CreditModificationRequest DTO
        Long targetUserId = request.getTargetUserId();
        Long amount = request.getAmount();

        // Get User
        User targetUser = userService.getUserWithId(targetUserId);
        Long currentCredit = targetUser.getCredit();

        if (!isAddition && currentCredit < amount) {
            throw new IllegalArgumentException("잔여 크레딧이 부족합니다.");
        }

        creditService.applyCreditChange(
                targetUser,
                currentCredit + (isAddition ? amount : -amount)
        );

        return ResponseEntity.ok().build();
    }
}
