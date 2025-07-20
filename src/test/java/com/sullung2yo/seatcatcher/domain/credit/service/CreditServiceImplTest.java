package com.sullung2yo.seatcatcher.domain.credit.service;

import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.domain.train.enums.YieldRequestType;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

    private static final long USER_ID = 1L;
    private static final long AMOUNT = 50L;
    private static final long INIT_CREDIT = 1_000L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private CreditServiceImpl creditService;      // 테스트 대상

    private User baseUser;

    @BeforeEach
    void setUp() {
        baseUser = User.builder()
                .credit(INIT_CREDIT)
                .build();
        ReflectionTestUtils.setField(baseUser, "id", USER_ID);
        baseUser.setUpdatedAt(LocalDateTime.now());

        // getUserWithId() mock
        when(userService.getUserWithId(USER_ID)).thenReturn(baseUser);
        // save() 호출 시 그대로 반환
        lenient().when(userRepository.save(Mockito.<User>any()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    // Helper : 저장된 User 의 크레딧 캡처
    private long captureSavedCredit() {
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue().getCredit();
    }

    @Test
    @DisplayName("좌석 생성 시 크레딧이 amount 만큼 증가해야 한다")
    void whenSeatCreated_thenCreditIncreases() {

        // when
        creditService.creditModification(USER_ID,
                AMOUNT,
                true,
                YieldRequestType.NONE);

        // then
        long savedCredit = captureSavedCredit();
        assertThat(savedCredit).isEqualTo(INIT_CREDIT + AMOUNT);
    }

    @Test
    @DisplayName("좌석 제거(5분 이내) 시 크레딧이 amount 만큼 차감돼야 한다")
    void whenSeatDeletedWithin5Min_thenCreditDecreases() {

        // 최근 갱신 시간 : 2분 전
        baseUser.setUpdatedAt(LocalDateTime.now().minusMinutes(2));

        // when
        creditService.creditModification(USER_ID,
                AMOUNT,
                false,
                YieldRequestType.NONE);

        // then
        long savedCredit = captureSavedCredit();
        assertThat(savedCredit).isEqualTo(INIT_CREDIT - AMOUNT);
    }

    @Test
    @DisplayName("양보 수락 시 크레딧이 amount 만큼 증가해야 한다")
    void whenSeatYieldAccepted_thenCreditIncreases() {

        creditService.creditModification(USER_ID,
                AMOUNT,
                true,
                YieldRequestType.ACCEPT);

        long savedCredit = captureSavedCredit();
        assertThat(savedCredit).isEqualTo(INIT_CREDIT + AMOUNT);
    }

    @Test
    @DisplayName("양보 요청 시 크레딧이 amount 만큼 차감돼야 한다")
    void whenSeatYieldRequested_thenCreditDecreases() {

        creditService.creditModification(USER_ID,
                AMOUNT,
                false,
                YieldRequestType.REQUEST);

        long savedCredit = captureSavedCredit();
        assertThat(savedCredit).isEqualTo(INIT_CREDIT - AMOUNT);
    }

    @Test
    @DisplayName("ACCEPT 타입에 차감 요청 시 예외가 발생해야 한다")
    void whenAcceptButReductionRequested_thenThrows() {

        assertThatThrownBy(() ->
                creditService.creditModification(USER_ID,
                        AMOUNT,
                        false,
                        YieldRequestType.ACCEPT)
        ).isInstanceOf(UserException.class);

        verify(userRepository, never()).save(Mockito.<User>any()); // DB 반영 없음
    }

    @Test
    @DisplayName("크레딧이 부족한 경우 예외가 발생해야 한다")
    void whenInsufficientCredit_thenThrows() {
        // given
        baseUser.setCredit(10L); // 충분하지 않은 크레딧 설정

        // when/then
        assertThatThrownBy(() ->
                creditService.creditModification(USER_ID,
                        AMOUNT,
                        false,
                        YieldRequestType.REQUEST)
        ).isInstanceOf(UserException.class);

        verify(userRepository, never()).save(Mockito.<User>any()); // DB 반영 없음
    }

    @Test
    @DisplayName("5분 초과 후 좌석 정보를 삭제하면 크레딧이 차감되지 않는다")
    void whenSeatDeletedAfterFiveMinutes_thenCreditNotDecreased() {
        // given – 5분 1초 이전으로 설정 (301초)
        baseUser.setUpdatedAt(LocalDateTime.now().minusMinutes(5).minusSeconds(1));

        // when
        creditService.creditModification(
                USER_ID,
                AMOUNT,
                /* isAddition = */ false,
                YieldRequestType.NONE);

        // then
        // 1) 저장 메서드가 호출되지 않았는지 확인
        verify(userRepository, never()).save(Mockito.<User>any());

        // 2) 엔티티의 크레딧이 그대로인지 확인
        assertThat(baseUser.getCredit()).isEqualTo(INIT_CREDIT);
    }
}