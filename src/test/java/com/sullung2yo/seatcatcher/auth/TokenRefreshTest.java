package com.sullung2yo.seatcatcher.auth;


import com.sullung2yo.seatcatcher.user.repository.RefreshTokenRepository;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenRefreshTest {
    // JwtTokenProviderImpl에 있는 refreshToken 메서드를 테스트하는 테스트 클래스

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

}
