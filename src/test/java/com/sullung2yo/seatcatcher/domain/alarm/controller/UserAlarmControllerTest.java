package com.sullung2yo.seatcatcher.domain.alarm.controller;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.alarm.entity.UserAlarm;
import com.sullung2yo.seatcatcher.domain.alarm.enums.PushNotificationType;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.alarm.repository.UserAlarmRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserAlarmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAlarmRepository userAlarmRepository;

    @Autowired
    private JwtTokenProviderImpl tokenProvider;

    private User testUser;
    private String accessToken;
    private UserAlarm testAlarm;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("alarmUser_test")
                .name("알람 유저")
                .credit(0L)
                .build());

        accessToken = tokenProvider.createToken(
                testUser.getProviderId(),
                Map.of("role", testUser.getRole().toString()),
                TokenType.ACCESS
        );

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
    void getMyAlarms_success() throws Exception {
        mockMvc.perform(get("/alarms/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userAlarmItemList[0].title").value("테스트 알람 제목"));
    }

    @Test
    @DisplayName("단일 알람 조회 성공 및 읽음 처리")
    void getAlarm_success() throws Exception {
        mockMvc.perform(get("/alarms/" + testAlarm.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAlarm.getId()))
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @DisplayName("알람 삭제 성공")
    void deleteAlarm_success() throws Exception {
        mockMvc.perform(delete("/alarms/" + testAlarm.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string("알람이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("토큰 없이 알람 목록 요청 시 401")
    void getMyAlarms_unauthorized() throws Exception {
        mockMvc.perform(get("/alarms/me")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("존재하지 않는 알람 조회 시 404")
    void getAlarm_notFound() throws Exception {
        mockMvc.perform(get("/alarms/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 사용자의 알람에 접근하는 경우 403 Forbidden 반환")
    void getAlarm_forbidden() throws Exception {
        // 다른 유저 생성 및 토큰 발급
        User otherUser = userRepository.save(User.builder()
                .provider(Provider.KAKAO)
                .providerId("other_user")
                .name("다른 유저")
                .credit(0L)
                .build());

        String otherToken = tokenProvider.createToken(
                otherUser.getProviderId(),
                Map.of("role", otherUser.getRole().toString()),
                TokenType.ACCESS
        );

        // 실제 알람은 testUser 소속
        mockMvc.perform(get("/alarms/" + testAlarm.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 알람 삭제 시 404")
    void deleteAlarm_notFound() throws Exception {
        mockMvc.perform(delete("/alarms/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("다른 사용자의 알람 삭제 시 403")
    void deleteAlarm_forbidden() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("other_user2")
                .name("다른유저2")
                .credit(0L)
                .build());

        String otherToken = tokenProvider.createToken(
                otherUser.getProviderId(),
                Map.of("role", otherUser.getRole().toString()),
                TokenType.ACCESS
        );

        mockMvc.perform(delete("/alarms/" + testAlarm.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }



    @AfterEach
    void tearDown() {
        userAlarmRepository.deleteAll();
        userRepository.deleteAll();
    }
}
