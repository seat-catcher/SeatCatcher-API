package com.sullung2yo.seatcatcher.domain.user_status.service;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user_status.entity.UserStatus;
import com.sullung2yo.seatcatcher.domain.user_status.dto.request.UserStatusRequest;
import com.sullung2yo.seatcatcher.domain.user_status.dto.response.UserStatusResponse;
import com.sullung2yo.seatcatcher.domain.user_status.repository.UserStatusRepository;
import com.sullung2yo.seatcatcher.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserStatusServiceImpl implements UserStatusService {

    private final UserService userService;
    private final UserStatusRepository userStatusRepository;

    @Override
    public UserStatusResponse getUserStatusWithToken(String token) throws RuntimeException {

        final User user = userService.getUserWithToken(token); // 일단 유저를 가져온다. 이는 토큰에서 추출함.

        UserStatus userStatusEntity = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UserException("사용자의 상태값을 찾을 수 없습니다.", ErrorCode.USER_STATUS_NOT_FOUND));

        return UserStatusResponse.builder()
                .trainCode(userStatusEntity.getTrainCode())
                .carCode(userStatusEntity.getCarCode())
                .seatSection(userStatusEntity.getSeatSection())
                .seatIdRequested(userStatusEntity.getSeatIdRequested())
                .build();
    }

    @Override
    public void createUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException {
        final User user = userService.getUserWithToken(token); // 일단 유저를 가져옵시다.

        Optional<UserStatus> previousEntity = userStatusRepository.findByUserId(user.getId());
        // 이미 Status 가 존재할 경우 우선적으로 제거가 이루어져야 함.
        previousEntity.ifPresent(userStatusRepository::delete);

        UserStatus userStatusEntity = UserStatus.builder()
                .trainCode(userStatusRequest.getTrainCode())
                .carCode(userStatusRequest.getCarCode())
                .seatSection(userStatusRequest.getSeatSection())
                .seatIdRequested(userStatusRequest.getSeatIdRequested())
                .user(user)
                .build();

        userStatusRepository.save(userStatusEntity);
    }

    @Override
    public void deleteUserStatus(User user) throws RuntimeException {
        Optional<UserStatus> entity = userStatusRepository.findByUserId(user.getId());
        entity.ifPresent(userStatusRepository::delete); // 있으면 제거함. 없으면 말고.
    }

    @Override
    public void updateUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException {
        final User user = userService.getUserWithToken(token); // 일단 유저를 가져옵시다.

        UserStatus entity = userStatusRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new UserException(
                                "사용자의 Status 를 찾을 수 없습니다.",
                                ErrorCode.USER_STATUS_NOT_FOUND)
                );

        if(userStatusRequest.getTrainCode() != null) entity.setTrainCode(userStatusRequest.getTrainCode());
        if(userStatusRequest.getCarCode() != null) entity.setCarCode(userStatusRequest.getCarCode());
        if(userStatusRequest.getSeatSection() != null) entity.setSeatSection(userStatusRequest.getSeatSection());
        if(userStatusRequest.getSeatIdRequested() != null) entity.setSeatIdRequested(userStatusRequest.getSeatIdRequested());
    }
}
