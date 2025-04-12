package com.sullung2yo.seatcatcher.user.utility.random;

import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class NameGeneratorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
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
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        user.setName(randomName);
        userRepository.save(user);

        // Then
        assertNotEquals(originalName, user.getName());
        verify(userRepository, times(1)).save(any(User.class)); // userRepository.save() 메서드가 한 번 호출되었는지 검증
    }
}