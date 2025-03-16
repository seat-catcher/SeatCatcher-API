package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.AuthReqeust;

import java.util.List;

public interface AuthService {
    public List<String> authenticate(AuthReqeust request) throws Exception;
}
