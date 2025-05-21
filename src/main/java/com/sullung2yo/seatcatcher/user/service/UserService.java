package com.sullung2yo.seatcatcher.user.service;


import com.sullung2yo.seatcatcher.train.domain.YieldRequestType;
import com.sullung2yo.seatcatcher.user.domain.CreditPolicy;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.UserInformationUpdateRequest;

public interface UserService {

    User getUserWithId(Long userId) throws RuntimeException;
    User getUserWithToken(String token) throws RuntimeException;
    User updateUser(String token, UserInformationUpdateRequest userInformationUpdateRequest) throws RuntimeException;
    void deleteUser(String token) throws RuntimeException;
}
