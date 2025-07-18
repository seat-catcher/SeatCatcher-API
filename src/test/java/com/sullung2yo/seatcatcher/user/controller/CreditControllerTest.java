package com.sullung2yo.seatcatcher.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.domain.ProfileImageNum;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.CreditModificationRequest;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CreditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper 객체

    private User user;

    private String accessToken;

    @BeforeEach
    void setUp() {
        // 1. 사용자 생성
        user = userRepository.save(
                User.builder()
                        .provider(Provider.APPLE)
                        .providerId("testProviderId")
                        .name("testUser")
                        .credit(123L)
                        .profileImageNum(ProfileImageNum.IMAGE_1)
                        .build()
        );

        // 2. 인증 토큰
        accessToken = jwtTokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void creditIncrease() throws Exception {
        Long beforeCredit = user.getCredit();
        Long amountToAdd = 100L;

        CreditModificationRequest request = new CreditModificationRequest();
        request.setAmount(amountToAdd);
        request.setTargetUserId(user.getId());

        mockMvc.perform(
                        patch("/credit")
                                .header("Authorization", "Bearer " + accessToken)
                                .param("isAddition", "true")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(beforeCredit + amountToAdd, updatedUser.getCredit());
    }

    @Test
    void creditDecrease() throws Exception {
        Long beforeCredit = user.getCredit();
        Long amountToMinus = 100L;

        CreditModificationRequest request = new CreditModificationRequest();
        request.setAmount(amountToMinus);
        request.setTargetUserId(user.getId());

        mockMvc.perform(
                        patch("/credit")
                                .header("Authorization", "Bearer " + accessToken)
                                .param("isAddition", "false")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(beforeCredit - amountToMinus, updatedUser.getCredit());
    }

    @Test
    void invalidCreditModificationRequest() throws Exception {
        // 잘못된 요청: amount가 음수인 경우
        CreditModificationRequest request = new CreditModificationRequest();
        request.setAmount(-50L); // 음수로 설정
        request.setTargetUserId(user.getId());

        mockMvc.perform(
                        patch("/credit")
                                .header("Authorization", "Bearer " + accessToken)
                                .param("isAddition", "true")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }
}