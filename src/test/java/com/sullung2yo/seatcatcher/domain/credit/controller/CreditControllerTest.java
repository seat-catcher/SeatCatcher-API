package com.sullung2yo.seatcatcher.domain.credit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.credit.dto.request.CreditModificationRequest;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
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

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtTokenProviderImpl jwtTokenProvider;
    @Autowired ObjectMapper objectMapper;

    private User user;
    private String accessToken;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.builder()
                        .provider(Provider.APPLE)
                        .providerId("testProviderId")
                        .name("testUser")
                        .credit(123L)
                        .profileImageNum(ProfileImageNum.IMAGE_1)
                        .build()
        );
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

        CreditModificationRequest request = CreditModificationRequest.builder()
                .amount(amountToAdd)
                .targetUserId(user.getId())
                .build();

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

        CreditModificationRequest request = CreditModificationRequest.builder()
                .amount(amountToMinus)
                .targetUserId(user.getId())
                .build();

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
        CreditModificationRequest request = CreditModificationRequest.builder()
                .amount(-50L)
                .targetUserId(user.getId())
                .build();

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