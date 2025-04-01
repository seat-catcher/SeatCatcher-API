package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.RefreshToken;
import com.sullung2yo.seatcatcher.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findRefreshTokenByUserAndRefreshToken(User user, String refreshToken);
}
