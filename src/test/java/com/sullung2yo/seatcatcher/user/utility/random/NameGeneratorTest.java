package com.sullung2yo.seatcatcher.user.utility.random;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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

    @Test
    void testGenerateRandomName() {
        // Given, When
        String randomName = nameGenerator.generateRandomName();
        log.debug("testGenerateRandomName: {}", randomName);

        // Then
        assertNotNull(randomName);
        assertNotEquals(originalName, randomName);
    }

    @Test
    void testGenerateRealUser() {
        // Given
        String randomName = nameGenerator.generateRandomName();
        User user = User.builder()
                .provider(Provider.KAKAO)
                .providerId("testProviderId")
                .name(originalName)
                .build();
        userRepository.save(user);

        // When
        user.setName(randomName);
        userRepository.save(user);

        // Then
        assertNotEquals(originalName, user.getName());
        userRepository.delete(user);
    }
}