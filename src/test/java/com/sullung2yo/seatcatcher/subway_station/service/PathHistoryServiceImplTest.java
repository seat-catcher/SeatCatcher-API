package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.domain.Line;
import com.sullung2yo.seatcatcher.subway_station.domain.PathHistory;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.repository.PathHistoryRepository;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayStationRepository;
import com.sullung2yo.seatcatcher.user.domain.Provider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.domain.UserRole;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest
@Transactional // 테스트 이후 롤백되게 함
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // H2 설정 유지
public class PathHistoryServiceImplTest {
    @Autowired
    private PathHistoryService pathHistoryService;

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

        PathHistoryRequest request = new PathHistoryRequest(user.getId(), startStation.getId(), endStation.getId());

        // when
        pathHistoryService.addPathHistory(request);

        // then
        List<PathHistory> histories = pathHistoryRepository.findAll();
        assertThat(histories).hasSize(1);
        assertThat(histories.get(0).getUser().getId()).isEqualTo(user.getId());
    }
}
