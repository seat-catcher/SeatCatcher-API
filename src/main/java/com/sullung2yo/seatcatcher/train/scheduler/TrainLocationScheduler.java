package com.sullung2yo.seatcatcher.train.scheduler;

import com.sullung2yo.seatcatcher.train.dto.response.LiveTrainLocationResponse;
import com.sullung2yo.seatcatcher.train.service.TrainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <a href="https://data.seoul.go.kr/dataList/OA-12764/F/1/datasetView.do">...</a>
 * 서울시 지하철 실시간 도착 정보를 주기적으로 호출하는 스케줄러
 * 30초마다 주기적으로 호출해서 일단 DB에 저장하도록 구현했습니다.
 * 나중에 Redis나 ElasticCache 같은거 사용해서 개선하면 좋을 것 같아요
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrainLocationScheduler {

    private final TrainService trainService;
    private static final List<String> lineNumbers = List.of("2호선", "7호선");

    @Scheduled(fixedRate = 10000) // 10초마다 실행
    public void fetchAndStoreTrainLocation() {
        // 서울시 지하철 실시간 도착 정보 조회
        log.debug("서울시 전체 지하철 실시간 열차 위치 정보 수집 작업 시작");
        for (String lineNumber : lineNumbers) {
            log.debug("지하철 {} 실시간 열차 위치 정보 수집 시작", lineNumber);
            List<LiveTrainLocationResponse> result = trainService.fetchLiveTrainLocation(lineNumber).block();
            if (result == null) {
                log.error("{} 실시간 열차 위치 정보 수집 작업 실패", lineNumber);
            } // null이 아니고, result가 Empty인 경우에는 정상 처리 => 새벽 시간대에는 아무 열차가 없으니까 정상 상태라고 보면 됨
            else {
                trainService.saveLiveTrainLocation(result);
            }
        }
    }

}
