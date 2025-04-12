package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.TrainSeat;
import com.sullung2yo.seatcatcher.train.domain.UserTrainSeat;
import com.sullung2yo.seatcatcher.train.dto.request.TrainSeatRequest;
import com.sullung2yo.seatcatcher.train.repository.TrainSeatRepository;
import com.sullung2yo.seatcatcher.train.repository.UserTrainSeatRepository;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTrainSeatServiceImpl implements UserTrainSeatService {

    private final UserTrainSeatRepository userTrainSeatRepository;
    private final TrainSeatRepository trainSeatRepository;
    private final UserRepository userRepository;

    private final TrainSeatService trainSeatService;

    @Override
    @Transactional
    public void create(Long userId, Long seatId) {
        User user = userRepository.findById(userId).orElseThrow(EntityNotFoundException::new);
        TrainSeat seat = trainSeatRepository.findById(seatId).orElseThrow(EntityNotFoundException::new);

        UserTrainSeat item = UserTrainSeat.builder()
                .user(user)
                .trainSeat(seat).build();
        userTrainSeatRepository.save(item);
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
    public void delete(Long userId) {
        UserTrainSeat item = userTrainSeatRepository.findUserTrainSeatByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);

        // 착석 정보를 제거했다면 당연히 좌석에 대한 찜 수라던가도 초기화가 진행되어야 함.
        trainSeatService.update(item.getTrainSeat().getId(), TrainSeatRequest.builder().jjimCount(0).build());

        userTrainSeatRepository.deleteUserTrainSeatByUserId(userId);
    }
}
