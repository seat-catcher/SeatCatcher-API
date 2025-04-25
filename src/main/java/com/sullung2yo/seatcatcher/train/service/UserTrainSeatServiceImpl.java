package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SeatException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import com.sullung2yo.seatcatcher.train.event_handler.SeatEventPublisher;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
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
    private final SeatEventPublisher seatEventPublisher;
    // private final SeatEventAssembler seatEventAssembler;

    @Override
    @Transactional
    public void reserveSeat(Long userId, Long seatId) {
        // 사용자 정보 가져오기
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserException("userId에 해당하는 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 좌석 정보 가져오기
        Optional<TrainSeat> seat = trainSeatRepository.findById(seatId);
        if (seat.isEmpty()) {
            throw new UserException("seatId에 해당하는 좌석을 찾을 수 없습니다.", ErrorCode.SEAT_NOT_FOUND);
        }

        // 좌석 점유 정보 생성 및 저장
        UserTrainSeat userSeat = UserTrainSeat.builder()
                .user(user.get())
                .trainSeat(seat.get()).build();
        userTrainSeatRepository.save(userSeat);

        // 좌석 예약 이벤트 발행
        // 1. SeatEvent 객체 생성
        // 2. seatEventPublisher.publish(seatEvent); // RabbitMQ에 발행
    }

    @Override
    public UserTrainSeat findUserTrainSeatByUserId(Long id) {
        return userTrainSeatRepository.findUserTrainSeatByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException("UserTrainSeat not found"));
    }

    @Override
    public UserTrainSeat findUserTrainSeatBySeatId(Long id) {
        return userTrainSeatRepository.findUserTrainSeatByTrainSeatId(id)
                .orElseThrow(() -> new EntityNotFoundException("UserTrainSeat not found"));
    }

    @Override
    @Transactional
    public void releaseSeat(Long userId) {
        // 사용자가 점유한 좌석 정보 찾아오기
        Optional<UserTrainSeat> userSeat = userTrainSeatRepository.findUserTrainSeatByUserId(userId);
        if (userSeat.isEmpty()) {
            throw new SeatException("해당 사용자는 좌석을 점유하고 있지 않습니다.", ErrorCode.USER_NOT_RESERVED);
        }

        // 해당 좌석 삭제
        userTrainSeatRepository.deleteUserTrainSeatByUserId(userId);

        // 업데이트된 좌석 정보 전달
        // SeatEvent 객체 만들어주는거 필요
        // seatEventPublisher.publish(seatEvent); SeatEvent 객체를 RabbitMQ에 전달
    }
}
