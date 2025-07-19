package com.sullung2yo.seatcatcher.domain.alarm.service;

import com.sullung2yo.seatcatcher.SeatCatcherApiApplication;
import com.sullung2yo.seatcatcher.domain.alarm.entity.UserAlarm;
import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import com.sullung2yo.seatcatcher.domain.user.domain.UserRole;
import com.sullung2yo.seatcatcher.domain.alarm.repository.UserAlarmRepository;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(classes = SeatCatcherApiApplication.class)
@Transactional
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserAlarmServiceImplTest {

    @Autowired
    private UserAlarmService userAlarmService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAlarmRepository userAlarmRepository;
    @Autowired private JwtTokenProviderImpl tokenProvider;

    private User testUser;
    private String accessToken;
    private UserAlarm testAlarm;

    @BeforeEach
    void setUp() {
        // 유저 저장
        testUser = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("alarmUser_test")
                .role(UserRole.ROLE_USER)
                .name("알람 유저")
                .credit(0L)
                .build());

        // 토큰 발급
        accessToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.ACCESS
        );

        // 알람 저장
        testAlarm = userAlarmRepository.save(UserAlarm.builder()
                .user(testUser)
                .type(PushNotificationType.SEAT_REQUEST_RECEIVED)
                .title("테스트 알람 제목")
                .body("테스트 알람 본문")
                .isRead(false)
                .build());
    }

    @Test
    @DisplayName("알람 목록 조회 성공")
    void getMyAlarms_success() {
        // when
        var response = userAlarmService.getMyAlarms(accessToken, 10, null, null, null);

        // then
        assertThat(response.getUserAlarmItemList()).hasSize(1);
        assertThat(response.getUserAlarmItemList().get(0).getTitle()).isEqualTo("테스트 알람 제목");
    }

    @Test
    @DisplayName("단일 알람 조회 시 읽음 처리")
    void getAlarm_marksAsRead() {
        // when
        var response = userAlarmService.getAlarm(accessToken, testAlarm.getId());

        // then
        assertThat(response.getTitle()).isEqualTo("테스트 알람 제목");
        assertThat(userAlarmRepository.findById(testAlarm.getId()).orElseThrow().isRead()).isTrue();
    }

    @Test
    @DisplayName("알람 삭제 성공")
    void deleteAlarm_success() {
        // when
        userAlarmService.deletAlarm(accessToken, testAlarm.getId());

        // then
        boolean exists = userAlarmRepository.existsById(testAlarm.getId());
        assertThat(exists).isFalse();
    }
}