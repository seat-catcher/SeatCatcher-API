package com.sullung2yo.seatcatcher.domain.auth.service;

import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;

import java.util.List;

public interface AuthService {
    public List<String> authenticate(Object request, Provider provider) throws Exception;
    public List<String> refreshToken(String token) throws Exception;
    public Boolean validateAccessToken(String token) throws Exception; // 토큰 유효성 검사 메서드
}
