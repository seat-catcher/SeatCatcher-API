package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserStatus;
import com.sullung2yo.seatcatcher.user.dto.request.UserStatusRequest;
import com.sullung2yo.seatcatcher.user.dto.response.UserStatusResponse;

public interface UserStatusService {

    UserStatusResponse getUserStatusWithToken(String token) throws RuntimeException;
    void createUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException;
    void deleteUserStatus(User user);
    void updateUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException;
}
