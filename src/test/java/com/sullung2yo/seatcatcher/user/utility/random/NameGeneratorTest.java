package com.sullung2yo.seatcatcher.user.utility.random;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class NameGeneratorTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NameGenerator nameGenerator;
    private static final String originalName = "originalName";

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGenerateRandomName() {
        // Given, When
        String randomName = nameGenerator.generateRandomName();
        log.debug(randomName);

        // Then
        assertNotNull(randomName);
        assertNotEquals(originalName, randomName);
    }

    @Test
    void testGenerateRealUser() {
        // Given
        User user = User.builder()
                .provider(Provider.KAKAO)
                .providerId("testProviderId")
                .name(nameGenerator.generateRandomName())
                .build();
        userRepository.save(user);
        String originUserName = user.getName();

        // When
        user.setName(nameGenerator.generateRandomName());
        userRepository.save(user);

        // Then
        assertNotEquals(originUserName, user.getName());
        userRepository.deleteAll();
    }
}