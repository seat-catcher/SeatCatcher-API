package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.User;

public interface UserService {

    User getUserWithToken(String token);
}
