package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByUserId(Long userId);
}
