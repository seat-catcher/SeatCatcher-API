package com.sullung2yo.seatcatcher.domain.credit.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * 크레딧을 증가시키는 메서드
     * @param userId 사용자 ID
     * @param amount 증가할 크레딧
     * @param isAddition 크레딧 증가 여부 True : 증가, False : 감소
     * @param yieldRequestType 요청 유형 (NONE/ACCEPT/REQUEST)
     */
    @Override
    @Transactional
    public void creditModification(Long userId, long amount, boolean isAddition, YieldRequestType yieldRequestType) throws RuntimeException {
        // 현재 크레딧 조회
        User user           = userService.getUserWithId(userId);
        long beforeAmount   = user.getCredit();
        long creditDelta    = 0;          // isAddition이 true면 증가, false면 감소
        LocalDateTime now   = LocalDateTime.now();

        // 요청별 증‧감 로직
        switch (yieldRequestType) {

            // 단순 좌석 생성/제거
            case NONE -> {
                if (isAddition) {                              // 좌석 생성 시
                    creditDelta =  amount;
                } else {                                       // 좌석 정보 제거 시
                    boolean within5Min = Duration.between(user.getUpdatedAt(), now).toSeconds() <= 300;
                    if (within5Min) {
                        creditDelta = -amount;                 // 5분 내 -> 회수
                    } else {
                        return;                                // 5분 초과 -> 변동 X
                    }
                }
            }

            // ACCEPT : 양보 수락 -> 무조건 증액
            case ACCEPT -> {
                if (!isAddition) {
                    throw new UserException("ACCEPT는 크레딧 감소가 불가합니다.", ErrorCode.INVALID_CREDIT_MODIFICATION);
                }
                creditDelta = amount;
            }

            // REJECT : 양보 거절당함 -> 무조건 증액 (복구)
            case REJECT -> {
                if(!isAddition) {
                    throw new UserException("REJECT는 크레딧 감소가 불가합니다.", ErrorCode.INVALID_CREDIT_MODIFICATION);
                }
                creditDelta = amount;
            }

            // CANCEL : 양보 요청을 취소함. -> 무조건 증액 (복구)
            case CANCEL -> {
                if(!isAddition) {
                    throw new UserException("CANCEL은 크레딧 감소가 불가합니다.", ErrorCode.INVALID_CREDIT_MODIFICATION);
                }
                creditDelta = amount;
            }

            // REQUEST : 양보 요청 -> 무조건 차감
            case REQUEST -> {
                if (isAddition) {
                    throw new UserException("REQUEST는 크레딧 증가가 불가합니다.",
                            ErrorCode.INVALID_CREDIT_MODIFICATION);
                }
                creditDelta = -amount;
            }

            default -> throw new UserException("지원하지 않는 요청 타입입니다.",
                    ErrorCode.INVALID_CREDIT_MODIFICATION);
        }

        // 최종 크레딧 계산 및 음수 방지
        long afterAmount = beforeAmount + creditDelta;
        if (afterAmount < 0) {
            throw new UserException("크레딧이 음수가 될 수 없습니다.",
                    ErrorCode.INSUFFICIENT_CREDIT);
        }

        // DB 반영
        User updatedUser = applyCreditChange(user, afterAmount);

        // 무결성 검증
        if (!isValidCreditModification(beforeAmount, updatedUser.getCredit(), amount, isAddition)) {
            log.debug("잘못된 크레딧 수정 시도");
            throw new UserException("좌석 정보에 대한 크레딧 수정 실패", ErrorCode.INVALID_CREDIT_MODIFICATION);
        }
    }

    @Transactional
    public User applyCreditChange(User user, long creditToUpdate) throws RuntimeException {

        if (creditToUpdate < 0) {
            throw new UserException(ErrorCode.INSUFFICIENT_CREDIT.getMessage(), ErrorCode.INSUFFICIENT_CREDIT);
        }

        log.debug("사용자 크레딧 업데이트: {}", creditToUpdate);
        user.setCredit(creditToUpdate);
        userRepository.save(user);

        return user;
    }

    /**
     * 크레딧 수정이 유효한지 검증하는 메서드
     * @param beforeAmount 수정 전 크레딧
     * @param afterAmount 수정 후 크레딧
     * @param isAddition 크레딧 증가 여부 True : 증가, False : 감소
     * @return 유효성 검사 결과
     */
    private boolean isValidCreditModification(long beforeAmount, long afterAmount, long amount, boolean isAddition) {
        if (isAddition) {
            return afterAmount - amount == beforeAmount;
        } else {
            return afterAmount + amount == beforeAmount;
        }
    }
}
