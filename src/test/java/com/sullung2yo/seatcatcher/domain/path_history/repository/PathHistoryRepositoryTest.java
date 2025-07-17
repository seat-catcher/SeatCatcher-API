package com.sullung2yo.seatcatcher.domain.path_history.repository;

import com.sullung2yo.seatcatcher.domain.subway_station.enums.Line;
import com.sullung2yo.seatcatcher.domain.path_history.entity.PathHistory;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional // 테스트 이후 자동 롤백
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2 설정 유지
@Slf4j
public class PathHistoryRepositoryTest {

    @Autowired
    private PathHistoryRepository pathHistoryRepository;

    @Autowired
    private SubwayStationRepository subwayStationRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private SubwayStation departure;
    private SubwayStation end;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .provider(Provider.APPLE)
                .providerId("user")
                .role(UserRole.ROLE_USER)
                .name("유저")
                .credit(0L)
                .build());

        departure = subwayStationRepository.save(SubwayStation.builder()
                .stationName("출발")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(100)
                .build());

        end = subwayStationRepository.save(SubwayStation.builder()
                .stationName("도착")
                .line(Line.LINE_2)
                .distance(0)
                .accumulateDistance(0)
                .timeMinSec("0:0")
                .accumulateTime(180)
                .build());
    }

    @Test
    @DisplayName("findTopByUserIdOrderByUpdatedAtDesc 가 가장 최신의 PathHistory 를 반환하는지 테스트")
    void findTopByUserIdOrderByUpdatedAtDesc_ShouldReturnLatestPathHistory()
    {
        //given
        PathHistory oldPathHistory = pathHistoryRepository.save(
                PathHistory.builder()
                        .user(user)
                        .startStation(departure)
                        .endStation(end)
                        .build()
        );

        PathHistory newPathHistory = pathHistoryRepository.save(
                PathHistory.builder()
                        .user(user)
                        .startStation(departure)
                        .endStation(end)
                        .build()
        );

        // when
        Optional<PathHistory> result = pathHistoryRepository.findTopByUserIdOrderByUpdatedAtDesc(user.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals(newPathHistory.getId(), result.get().getId());
    }
}
