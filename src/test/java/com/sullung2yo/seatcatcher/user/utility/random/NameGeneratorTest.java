package com.sullung2yo.seatcatcher.user.utility.random;

import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class NameGeneratorTest {

    private UserRepository userRepository;
    private NameGenerator nameGenerator;

    @BeforeEach
    void setUp() {
        nameGenerator = new NameGenerator();
    }

    @Test
    void testGenerateRandomName() {
        // Given
        String randomName = "originalName";

        // When
        randomName = nameGenerator.generateRandomName();
        log.debug(randomName);

        // Then
        assertNotNull(randomName);
        assertNotEquals("originalName", randomName);
    }
}