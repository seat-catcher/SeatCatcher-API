package com.sullung2yo.seatcatcher.user.service;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.config.exception.UserException;
import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProviderImpl jwtTokenProvider;

    @Override
    public User getUserWithToken(String token) {
        // 1. 토큰 검증
        jwtTokenProvider.validateToken(token, TokenType.ACCESS);

        // 2. 토큰에서 사용자 정보 추출
        String providerId = jwtTokenProvider.getProviderIdFromToken(token);
        if (providerId == null || providerId.isEmpty()) {
            throw new TokenException("토큰에서 사용자 정보를 추출할 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        // 3. 사용자 정보 DB에서 조회 후 반환
        Optional<User> user = userRepository.findByProviderId(providerId);
        if (user.isEmpty()) {
            throw new UserException("사용자를 찾을 수 없습니다.", ErrorCode.USER_NOT_FOUND);
        }

        return user.get();
    }
}
