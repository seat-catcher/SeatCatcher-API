package com.sullung2yo.seatcatcher.domain.user.service;


import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.dto.request.UserInformationUpdateRequest;

public interface UserService {

    User getUserWithId(Long userId) throws RuntimeException;
    User getUserWithToken(String token) throws RuntimeException;
    User updateUser(String token, UserInformationUpdateRequest userInformationUpdateRequest) throws RuntimeException;
    void deleteUser(String token) throws RuntimeException;
}
