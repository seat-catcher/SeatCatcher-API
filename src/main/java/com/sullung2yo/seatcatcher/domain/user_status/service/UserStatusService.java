package com.sullung2yo.seatcatcher.domain.user_status.service;

import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user_status.dto.request.UserStatusRequest;
import com.sullung2yo.seatcatcher.domain.user_status.dto.response.UserStatusResponse;

public interface UserStatusService {

    UserStatusResponse getUserStatusWithToken(String token) throws RuntimeException;
    void createUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException;
    void deleteUserStatus(User user);
    void updateUserStatus(String token, UserStatusRequest userStatusRequest) throws RuntimeException;
}
