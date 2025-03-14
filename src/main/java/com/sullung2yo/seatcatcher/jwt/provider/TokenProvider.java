package com.sullung2yo.seatcatcher.jwt.provider;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;

import java.util.Map;

public interface TokenProvider {

    public String createToken(String email, Map<String, ?> payload, TokenType tokenType);

}
