package com.sullung2yo.seatcatcher.domain.user.repository;

import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.enums.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // JPA 테스트 시 더 가볍고 빠르게 테스트 가능하다 -> @Entity 클래스만 스캔하므로 @Service, @Component, @Repository 등은 스캔 X
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableJpaAuditing
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager entityManager;

    private static final String APPLE_PROVIDER_ID = "test1234";
    private static final String KAKAO_PROVIDER_ID = "1234test";

    @BeforeEach
    void setUp() {
        User appleUser = User.builder()
                .provider(Provider.APPLE)
                .providerId(APPLE_PROVIDER_ID)
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();
        userRepository.save(appleUser);

        User kakaoUser = User.builder()
                .provider(Provider.KAKAO)
                .providerId(KAKAO_PROVIDER_ID)
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
                .credit(12345L)
                .build();

        userRepository.save(test);

        Optional<User> foundUser = userRepository.findByProviderId(providerId);
        assertTrue(foundUser.isPresent());
        assertEquals(providerId, foundUser.get().getProviderId());
    }

    @Test
    void createNegativeCreditUser() {
        //Given
        String providerId = "hello123";
        User test = User.builder()
                .provider(Provider.APPLE)
                .providerId(providerId)
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(-12345L)
                .build();

        //When, Then
        assertThrows(ConstraintViolationException.class, () -> {
            try {
                userRepository.save(test);
                entityManager.flush();
            } finally {
                entityManager.clear();
            }
        });

        assertTrue(userRepository.findByProviderId(providerId).isEmpty());
    }

    @Test
    void createDuplicateAppleUser() {
        Optional<User> foundUser = userRepository.findByProviderId(APPLE_PROVIDER_ID);
        assertTrue(foundUser.isPresent());
        assertEquals(APPLE_PROVIDER_ID, foundUser.get().getProviderId());

        User duplicate_user = User.builder()
                .provider(Provider.APPLE)
                .providerId(APPLE_PROVIDER_ID)
                .role(UserRole.ROLE_USER)
                .name("Apple USER")
                .lastLoginAt(LocalDateTime.now())
                .credit(300L)
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            try {
                userRepository.save(duplicate_user);
                entityManager.flush();
            } finally {
                entityManager.clear();
            }
        });
    }

    @Test
    void findByInvalidProviderId() {
        assertFalse(userRepository.findByProviderId("invalid").isPresent());
    }

    @Test
    void findByProviderId() {
        Optional<User> apple = userRepository.findByProviderId(APPLE_PROVIDER_ID);
        assertTrue(apple.isPresent());
        assertEquals(APPLE_PROVIDER_ID, apple.get().getProviderId());

        Optional<User> kakao = userRepository.findByProviderId(KAKAO_PROVIDER_ID);
        assertTrue(kakao.isPresent());
        assertEquals(KAKAO_PROVIDER_ID, kakao.get().getProviderId());
    }

}