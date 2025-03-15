package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.dto.request.TokenRequest;

import java.util.List;

public interface AuthService {
    public List<String> authenticate(Provider provider, TokenRequest request) throws Exception;
}
