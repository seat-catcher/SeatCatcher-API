package com.sullung2yo.seatcatcher.domain.auth.repository;

import com.sullung2yo.seatcatcher.domain.auth.entity.RefreshToken;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findRefreshTokenByUserAndRefreshToken(User user, String refreshToken);
}
