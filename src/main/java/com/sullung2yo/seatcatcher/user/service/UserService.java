package com.sullung2yo.seatcatcher.user.service;


import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.UserInformationUpdateRequest;

public interface UserService {

    User getUserWithId(Long userId) throws RuntimeException;
    User getUserWithToken(String token) throws RuntimeException;
    User updateUser(String token, UserInformationUpdateRequest userInformationUpdateRequest) throws RuntimeException;
    void deleteUser(String token) throws RuntimeException;

    // Endpoint용 Service.
    User increaseCredit(String token, long amount) throws RuntimeException;
    User decreaseCredit(String token, long amount) throws RuntimeException;
    // 내부 로직용 Service. 로직 중간에 크레딧 변경이 일어나야 할 경우 해당 서비스 이용하면 됨.
    User increaseCredit(User user, long amount) throws RuntimeException;
    User decreaseCredit(User user, long amount) throws RuntimeException;
}
