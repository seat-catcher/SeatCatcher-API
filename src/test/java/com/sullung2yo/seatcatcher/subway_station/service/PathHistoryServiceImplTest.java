package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.jwt.domain.TokenType;
import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@Transactional // 테스트 이후 롤백되게 함
@Slf4j
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2 설정 유지
public class PathHistoryServiceImplTest {
    @Autowired
    private PathHistoryService pathHistoryService;

    private JwtTokenProviderImpl tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private PathHistoryRepository pathHistoryRepository;

    @Test
    @DisplayName("PathHistory가 저장")

    void addPathHistory_realDbTest() {
        // given
        User user = userRepository.save(User.builder().provider(Provider.APPLE).providerId("tsetUser").role(UserRole.ROLE_USER).name("테스트 유저").credit(0L).build());
        SubwayStation startStation = subwayStationRepository.save(SubwayStation.builder().stationName("출발역").line(Line.LINE_2).distance(0).accumulateDistance(0).timeMinSec("0:0").accumulateTime(100).build());
        SubwayStation endStation = subwayStationRepository.save(SubwayStation.builder().stationName("도착역").line(Line.LINE_2).distance(0).accumulateDistance(0).timeMinSec("0:0").accumulateTime(180).build());

        String accessToken = tokenProvider.createToken(
                user.getProviderId(),
                Map.of("role", user.getRole().toString()),
                TokenType.ACCESS
        );

        PathHistoryRequest request = new PathHistoryRequest(startStation.getId(), endStation.getId());

        // when
        pathHistoryService.addPathHistory(accessToken, request);

        // then
        List<PathHistory> histories = pathHistoryRepository.findAll();
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("단건 PathHistory 조회")
    void getPathHistory() {
        // given
        User user = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("testUser2")
                .role(UserRole.ROLE_USER)
                .name("단건 조회 유저")
                .credit(0L)
                .build());

        SubwayStation startStation = subwayStationRepository.save(SubwayStation.builder()
                .stationName("출발역2")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build());

        SubwayStation endStation = subwayStationRepository.save(SubwayStation.builder()
                .stationName("도착역2")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build());

        PathHistory pathHistory = pathHistoryRepository.save(
                PathHistory.builder()
                        .user(user)
                        .startStation(startStation)
                        .endStation(endStation)
                        .build()
        );

        // 테스트용 인증 객체 설정 (SecurityContext)
        TestingAuthenticationToken auth = new TestingAuthenticationToken(user.getProviderId(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        PathHistoryResponse.PathHistoryInfoResponse result = pathHistoryService.getPathHistory(pathHistory.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartStationName()).isEqualTo("출발역2"); // startStation
        assertThat(result.getEndStationName()).isEqualTo("도착역2");  // endStation
    }
    @Test
    @DisplayName("다른 사용자가 PathHistory에 접근하면 예외 발생")
    void getPathHistory_forbiddenUser() {
        // given
        // 실제 PathHistory를 만든 유저
        User ownerUser = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("ownerUser")
                .role(UserRole.ROLE_USER)
                .name("소유자")
                .credit(0L)
                .build());

        // 요청을 시도할 다른 유저
        User otherUser = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("otherUser")
                .role(UserRole.ROLE_USER)
                .name("다른 유저")
                .credit(0L)
                .build());

        SubwayStation startStation = subwayStationRepository.save(SubwayStation.builder()
                .stationName("출발역")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build());

        SubwayStation endStation = subwayStationRepository.save(SubwayStation.builder()
                .stationName("도착역")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build());

        PathHistory pathHistory = pathHistoryRepository.save(
                PathHistory.builder()
                        .user(ownerUser)
                        .startStation(startStation)
                        .endStation(endStation)
                        .build()
        );

        // 요청자는 otherUser로 설정
        TestingAuthenticationToken auth = new TestingAuthenticationToken(otherUser.getProviderId(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        com.sullung2yo.seatcatcher.common.exception.SubwayException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        com.sullung2yo.seatcatcher.common.exception.SubwayException.class,
                        () -> pathHistoryService.getPathHistory(pathHistory.getId())
                );

        assertThat(exception.getErrorCode()).isEqualTo(
                com.sullung2yo.seatcatcher.common.exception.ErrorCode.PATH_HISTORY_FORBIDDEN
        );
        assertThat(exception.getMessage()).contains("해당 경로 이력에 접근할 권한이 없습니다.");
    }
    @Test
    @DisplayName("사용자의 PathHistory를 커서 기반으로 페이징 조회한다")
    void getAllPathHistory_cursorPagingTest() {
        // given
        User user = userRepository.save(
                User.builder()
                        .provider(Provider.APPLE)
                        .providerId("testUser_cursor")
                        .role(UserRole.ROLE_USER)
                        .name("테스트 유저")
                        .credit(0L)
                        .build()
        );

        SubwayStation startStation = subwayStationRepository.save(
                SubwayStation.builder()
                        .stationName("출발역")
                        .line(Line.LINE_2)
                        .distance(0)
                        .accumulateDistance(0)
                        .timeMinSec("0:0")
                        .accumulateTime(100)
                        .build()
        );

        SubwayStation endStation = subwayStationRepository.save(
                SubwayStation.builder()
                        .stationName("도착역")
                        .line(Line.LINE_2)
                        .distance(0)
                        .accumulateDistance(0)
                        .timeMinSec("0:0")
                        .accumulateTime(180)
                        .build()
        );

        // 데이터 여러 개 삽입
        for (int i = 0; i < 5; i++) {
            PathHistory pathHistory = PathHistory.builder()
                    .user(user)
                    .startStation(startStation)
                    .endStation(endStation)
                    .build();
            pathHistoryRepository.save(pathHistory);
        }

        TestingAuthenticationToken auth = new TestingAuthenticationToken(user.getProviderId(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        PathHistoryResponse.PathHistoryList response1 = pathHistoryService.getAllPathHistory(3, null);

        // then
        assertThat(response1.getPathHistoryInfoList()).hasSize(3);
//        log.info("PathHistoryInfo {}", response1.getPathHistoryInfoList());
        assertThat(response1.isLast()).isFalse(); // 더 있을 경우
//        log.info("cursorId {}", response1.getNextCursor());
    }

    @Test
    @DisplayName("사용자가 자신의 PathHistory를 정상적으로 삭제")
    void deletePathHistory_success() {
        // given
        User user = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("deleteUser")
                .role(UserRole.ROLE_USER)
                .name("삭제 유저")
                .credit(0L)
                .build());

        SubwayStation start = subwayStationRepository.save(SubwayStation.builder()
                .stationName("출발")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build());

        SubwayStation end = subwayStationRepository.save(SubwayStation.builder()
                .stationName("도착")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build());

        PathHistory pathHistory = pathHistoryRepository.save(
                PathHistory.builder()
                        .user(user)
                        .startStation(start)
                        .endStation(end)
                        .build()
        );

        TestingAuthenticationToken auth = new TestingAuthenticationToken(user.getProviderId(), null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        pathHistoryService.deletPathHistory(pathHistory.getId());

        // then
        boolean exists = pathHistoryRepository.existsById(pathHistory.getId());
        assertThat(exists).isFalse();
    }

}
