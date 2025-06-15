package com.sullung2yo.seatcatcher.subway_station.service;

public interface TransactionalExecuteService {
    void executeTransactional(Runnable transactionalAction);
}
