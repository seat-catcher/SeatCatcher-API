package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.user.domain.Provider;

import java.util.List;

public interface AuthService {
    public List<String> authenticate(Object request, Provider provider) throws Exception;
}
