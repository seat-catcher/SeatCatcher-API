package com.sullung2yo.seatcatcher.domain.train.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SeatException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeatGroup;
import com.sullung2yo.seatcatcher.domain.train.entity.TrainSeat;
import com.sullung2yo.seatcatcher.domain.train.entity.UserTrainSeat;
import com.sullung2yo.seatcatcher.domain.train.enums.YieldRequestType;
import com.sullung2yo.seatcatcher.domain.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.domain.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.credit.service.CreditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTrainSeatServiceImpl implements UserTrainSeatService {

    private final UserTrainSeatRepository userTrainSeatRepository;
    private final TrainSeatRepository trainSeatRepository;
    private final UserRepository userRepository;
    private final CreditService creditService;

    @Override
    @Transactional
    public UserTrainSeat reserveSeat(Long userId, Long seatId) { // TODO :: 테스트코드 작성 필요
        // 사용자 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(userId + "에 해당하는 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        // 좌석 정보 가져오기 (Lock 획득)
        TrainSeat seat = trainSeatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> {
                    log.error("좌석 예약 요청 실패: 사용자 ID={}, 좌석 ID={}", userId, seatId);
                    return new SeatException(seatId + "에 해당하는 좌석을 찾을 수 없습니다.", ErrorCode.SEAT_NOT_FOUND);
                });

        // 해당 사용자가 예약한 좌석이 이미 있는지 확인
        Optional<UserTrainSeat> hasUserReservedOtherSeat = userTrainSeatRepository.findUserTrainSeatByUserId(userId);
        if (hasUserReservedOtherSeat.isPresent()) {
            throw new SeatException("이미 다른 좌석을 예약한 사용자입니다.", ErrorCode.USER_ALREADY_RESERVED);
        }

        // 다른 사용자가 해당 좌석을 점유중인지 확인
        Optional<UserTrainSeat> hasOtherUserAlreadyReserved = userTrainSeatRepository.findUserTrainSeatByTrainSeatId(seatId);
        if (hasOtherUserAlreadyReserved.isPresent()) {
            throw new SeatException("해당 좌석은 다른 사용자가 점유중입니다.", ErrorCode.SEAT_ALREADY_RESERVED);
        }

        // 좌석 점유 정보 생성 및 저장 (매핑 생성 -> 매핑 생성되었으면 좌석을 점유했다고 생각하면 됨)
        UserTrainSeat userSeat = UserTrainSeat.builder()
                .user(user)
                .trainSeat(seat).build();
        userTrainSeatRepository.save(userSeat);
        return userSeat;
    }

    @Override
    public UserTrainSeat findUserTrainSeatByUserId(Long userId) {
        return userTrainSeatRepository.findUserTrainSeatByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("UserTrainSeat not found"));
    }

    @Override
    public UserTrainSeat findUserTrainSeatBySeatId(Long seatId) {
        return userTrainSeatRepository.findUserTrainSeatByTrainSeatId(seatId)
                .orElseThrow(() -> new EntityNotFoundException("UserTrainSeat not found"));
    }

    @Override
    @Transactional
    public void updateSeatOwner(Long userId, Long seatId, Long creditAmount) {
        // 1. 원래 좌석 소유자 정보 가져오기
        UserTrainSeat seat = findUserTrainSeatBySeatId(seatId);
        User originalUser = seat.getUser();

        // 2. 좌석 소유자 정보 업데이트
        User newUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(userId + "에 해당하는 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));
        seat.setUser(newUser);

        // 3. 원래 좌석 소유자에게 크레딧 지급
        try {
            creditService.creditModification(
                    originalUser.getId(),
                    //CreditPolicy.CREDIT_FOR_SEAT_YIELD_APPROVE.getCredit(),
                    creditAmount,
                    true,
                    YieldRequestType.ACCEPT
            );
        } catch (UserException e) {
            log.error("좌석 양도 승인 중 크레딧 지급 실패: {}", e.getMessage());
            throw e;
        }
        // 4. DB 저장 -> JPA가 자동으로 처리 (Transactional 어노테이션)
    }

    @Override
    @Transactional
    public TrainSeatGroup releaseSeat(Long userId) { // TODO :: 테스트코드 작성 필요
        // 사용자가 점유한 좌석 정보 찾아오기
        UserTrainSeat userSeat = userTrainSeatRepository.findUserTrainSeatByUserId(userId)
                .orElseThrow(() -> new SeatException("해당 사용자는 좌석을 점유하고 있지 않습니다.", ErrorCode.USER_NOT_RESERVED));

        // 열차 정보 가져오기
        TrainSeatGroup trainSeatGroup = userSeat.getTrainSeat().getTrainSeatGroup();
        if (trainSeatGroup == null) {
            throw new SeatException("열차 정보가 존재하지 않습니다. 내부 로직 오류 (열차 정보가 없어서는 안됨)", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 좌석 점유 정보 데이터베이스에서 삭제
        // 매핑테이블만 끊어버리면 됨
        log.info("좌석 해제 요청: 사용자 ID={}, 좌석 ID={}", userId, userSeat.getTrainSeat().getId());
        userTrainSeatRepository.deleteUserTrainSeatByUserId(userId);
        log.debug("좌석 해제 완료: 사용자 ID={}", userId);

        return trainSeatGroup;
    }

    @Override
    @Transactional
    public void yieldSeat(Long seatId, Long giverId, Long takerId) {
        releaseSeat(giverId);
        reserveSeat(takerId, seatId);
        //TODO :: 함수 구조를 이렇게 해서 이벤트가 두 번 발생할 것으로 예상됩니다. 추후 수정이 필요할 수도 있습니다.
    }

    @Override
    public boolean isUserSitting(Long userId) {
        Optional<UserTrainSeat> optional = userTrainSeatRepository.findUserTrainSeatByUserId(userId);
        return optional.isPresent();
    }
}