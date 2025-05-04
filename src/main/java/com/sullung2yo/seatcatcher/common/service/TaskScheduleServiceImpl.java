package com.sullung2yo.seatcatcher.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskScheduleServiceImpl implements TaskScheduleService {

    // 해당 파일은 단순 기능 구현 파일입니다. 인터페이스 사용법은 Interface 파일에 기록되어 있습니다.

    private final TaskScheduler taskScheduler;

    @Override
    public void runThisAfterSeconds(int seconds, Runnable task) {
        try {
            Instant triggerTime = LocalDateTime.now().plusSeconds(seconds)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            taskScheduler.schedule(task, triggerTime);
        } catch (Exception e) {
            log.error("Error scheduling task after {} seconds", seconds, e);
        }
    }

    @Override
    public void runThisAfterMinutes(int minutes, Runnable task) {
        try{
            Instant triggerTime = LocalDateTime.now().plusMinutes(minutes)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            taskScheduler.schedule(task, triggerTime);
        } catch (Exception e) {
            log.error("Error scheduling task after {} minutes", minutes, e);
        }
    }

    @Override
    public void runThisAtBeforeSeconds(LocalDateTime stdTime, int seconds, Runnable task) {
        try {
            Instant triggerTime = stdTime.minusSeconds(seconds)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            taskScheduler.schedule(task, triggerTime);
        } catch (Exception e) {
            log.error("Error scheduling task before {} seconds of {}", seconds, stdTime, e);
        }
    }

    @Override
    public void runThisAtBeforeMinutes(LocalDateTime stdTime, int minutes, Runnable task) {
        try{
        Instant triggerTime = stdTime.minusMinutes(minutes)
                .atZone(ZoneId.systemDefault())
                .toInstant();
        taskScheduler.schedule(task, triggerTime);
        } catch (Exception e) {
            log.error("Error scheduling task before {} minutes of {}", minutes, stdTime, e);
        }
    }
}
