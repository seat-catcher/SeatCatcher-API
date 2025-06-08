package com.sullung2yo.seatcatcher.subway_station.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionalExecuteServiceImpl implements TransactionalExecuteService {
    @Override
    @Transactional
    public void executeTransactional(Runnable transactionalAction) {
        transactionalAction.run();
    }
}
