package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.dto.request.AuthRequest;

import java.util.List;

public interface AuthService {
    public List<String> authenticate(AuthRequest request) throws Exception;
}
