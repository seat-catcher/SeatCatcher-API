package com.sullung2yo.seatcatcher.user.repository;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    String appleProviderId = "test1234";
    String kakaoProviderId = "1234test";

    @BeforeEach
    void setUp() {
        User appleUser = User.builder()
                .provider(Provider.APPLE)
                .providerId(appleProviderId)
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();
        userRepository.save(appleUser);

        User kakaoUser = User.builder()
                .provider(Provider.KAKAO)
                .providerId(kakaoProviderId)
                .role(UserRole.ROLE_USER)
                .name("Kakao USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();
        userRepository.save(kakaoUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void createAppleUser() {
        String providerId = "hello123";
        User test = User.builder()
                .provider(Provider.APPLE)
                .providerId(providerId)
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();

        userRepository.save(test);

        Optional<User> foundedUser = userRepository.findByProviderId(providerId);
        assertTrue(foundedUser.isPresent());
        assertEquals(providerId, foundedUser.get().getProviderId());
    }

    @Test
    void createDuplicateAppleUser() {
        Optional<User> foundedUser = userRepository.findByProviderId(appleProviderId);
        assertTrue(foundedUser.isPresent());
        assertEquals(appleProviderId, foundedUser.get().getProviderId());

        User duplicate_user = User.builder()
                .provider(Provider.APPLE)
                .providerId("test1234")
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(duplicate_user));
    }

    @Test
    void findByInvalidProviderId() {
        assertFalse(userRepository.findByProviderId("invalid").isPresent());
    }

    @Test
    void findByProviderId() {
        Optional<User> apple = userRepository.findByProviderId(appleProviderId);
        assertTrue(apple.isPresent());
        assertEquals(appleProviderId, apple.get().getProviderId());

        Optional<User> kakao = userRepository.findByProviderId(kakaoProviderId);
        assertTrue(kakao.isPresent());
        assertEquals(kakaoProviderId, kakao.get().getProviderId());
    }

}