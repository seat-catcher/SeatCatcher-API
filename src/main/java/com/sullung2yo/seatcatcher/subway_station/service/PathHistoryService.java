package com.sullung2yo.seatcatcher.subway_station.service;

import com.sullung2yo.seatcatcher.subway_station.dto.request.PathHistoryRequest;
import com.sullung2yo.seatcatcher.subway_station.dto.response.PathHistoryResponse;

public interface PathHistoryService {
    void addPathHistory(PathHistoryRequest request);
    PathHistoryResponse.PathHistoryInfoResponse getPathHistory(Long pathId);

    PathHistoryResponse.PathHistoryList getAllPathHistory(int size, Long pathId);

    void deletPathHistory(Long pathId);
}
