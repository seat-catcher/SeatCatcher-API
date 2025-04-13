package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.dto.response.LiveTrainLocationResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TrainService {
    /**
     * 지정한 lineNumber에 해당하는 실시간 열차 위치 정보를 조회하는 메서드
     * 예를 들어, lineNumber에 7호선이 들어오면, 실시간으로 7호선에 배치된 모든 열차의 정보를 가져오게 됩니다.
     * @param lineNumber 지하철 노선 번호 (ex: 1호선, 2호선, 3호선 ...)
     * @return 실시간 열차 위치 정보 리스트 (LiveTrainLocationResponse 참고)
     */
    Mono<List<LiveTrainLocationResponse>> fetchLiveTrainLocation(String lineNumber);

    /**
     * 서울시 지하철 실시간 도착 정보를 DB에 저장하는 메서드
     */
    void saveLiveTrainLocation();
}
