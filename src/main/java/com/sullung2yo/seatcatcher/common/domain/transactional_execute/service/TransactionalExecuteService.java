package com.sullung2yo.seatcatcher.common.domain.transactional_execute.service;

public interface TransactionalExecuteService {
    void executeTransactional(Runnable transactionalAction);
}
