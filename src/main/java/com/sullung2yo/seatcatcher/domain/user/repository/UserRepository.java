package com.sullung2yo.seatcatcher.domain.user.repository;

import com.sullung2yo.seatcatcher.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByFcmToken(String fcmToken);
}
