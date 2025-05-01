package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SeatException;
import com.sullung2yo.seatcatcher.common.exception.TrainException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.train.domain.Train;
import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.event.SeatEvent;
import com.sullung2yo.seatcatcher.train.event_handler.SeatEventAssembler;
import com.sullung2yo.seatcatcher.train.event_handler.SeatEventPublisher;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
    private final SeatEventAssembler seatEventAssembler;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void reserveSeat(Long userId, Long seatId) {

        // 좌석 예약 로직 수행
        reserve(userId, seatId);

        // 좌석 예약 이벤트 Publish
        // seatId를 사용해서 좌석 점유 이벤트를 발생시켰으므로
        // 새로운 SeatEvent 객체를 만들어서 RabbitMQ에 발행
        // 이때, SeatEvent 객체는 seatId를 사용해서 train정보를 가져온 뒤,
        // 모든 관련 정보를 담게 된다.
        Train train = trainSeatRepository.findTrainByTrainSeatId(seatId)
                .orElseThrow(() -> new TrainException("해당 열차를 찾을 수 없습니다.", ErrorCode.TRAIN_NOT_FOUND));
        SeatEvent event = seatEventAssembler.assembleSeatEvents(train);
        seatEventPublisher.publish(event); // RabbitMQ에 발행
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
        UserTrainSeat userSeat = userTrainSeatRepository.findUserTrainSeatByUserId(userId)
                .orElseThrow(() -> new SeatException("해당 사용자는 좌석을 점유하고 있지 않습니다.", ErrorCode.USER_NOT_RESERVED));

        // 열차 정보 가져오기
        Train train = userSeat.getTrainSeat().getTrain();
        if (train == null) {
            throw new SeatException("열차 정보가 존재하지 않습니다. 내부 로직 오류 (열차 정보가 없어서는 안됨)", ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 좌석 점유 정보 데이터베이스에서 삭제
        // 매핑테이블만 끊어버리면 됨
        userTrainSeatRepository.deleteUserTrainSeatByUserId(userId);

        // 업데이트된 좌석 정보 전달
        SeatEvent event = seatEventAssembler.assembleSeatEvents(train);
        seatEventPublisher.publish(event);
    }

    private void reserve(Long userId, Long seatId) {
        // 사용자 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("userId에 해당하는 사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND));

        // 좌석 정보 가져오기 (Lock 획득)
        TrainSeat seat = trainSeatRepository.findByIdForUpdate(seatId)
                .orElseThrow(() -> new SeatException("seatId에 해당하는 좌석을 찾을 수 없습니다.", ErrorCode.SEAT_NOT_FOUND));

        // 해당 사용자가 예약한 좌석이 이미 있는지 확인
        Optional<UserTrainSeat> hasUserReservedAlready = userTrainSeatRepository.findUserTrainSeatByUserId(userId);
        if (hasUserReservedAlready.isPresent()) {
            throw new SeatException("이미 다른 좌석을 예약한 사용자입니다.", ErrorCode.USER_ALREADY_RESERVED);
        }

        // 다른 사용자가 해당 좌석을 점유중인지 확인
        Optional<UserTrainSeat> hasAlreadyReserved = userTrainSeatRepository.findUserTrainSeatByTrainSeatId(seatId);
        if (hasAlreadyReserved.isPresent()) {
            throw new SeatException("해당 좌석은 다른 사용자가 점유중입니다.", ErrorCode.SEAT_ALREADY_RESERVED);
        }

        // 좌석 점유 정보 생성 및 저장 (매핑 생성 -> 매핑 생성되었으면 좌석을 점유했다고 생각하면 됨)
        UserTrainSeat userSeat = UserTrainSeat.builder()
                .user(user)
                .trainSeat(seat).build();
        userTrainSeatRepository.save(userSeat);
        entityManager.flush(); // 트랜잭션이 끝나기 전에 DB에 반영되도록 강제 호출
    }

    @Override
    @Transactional
    public void yieldSeat(Long seatId, Long giverID, Long takerId) {
        releaseSeat(giverID);
        reserveSeat(takerId, seatId);
        //TODO :: 함수 구조를 이렇게 해서 이벤트가 두 번 발생할 것으로 예상됩니다. 추후 수정이 필요할 수도 있습니다.
    }
}
