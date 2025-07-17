package com.sullung2yo.seatcatcher.common.domain.task_schedule.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskScheduleServiceImpl implements TaskScheduleService {

    // 해당 파일은 단순 기능 구현 파일입니다. 인터페이스 사용법은 Interface 파일에 기록되어 있습니다.

    private final TaskScheduler taskScheduler;

    @Override
    public LocalDateTime runThisAfterSeconds(long seconds, Runnable task) {
        try {
            LocalDateTime triggerTime = LocalDateTime.now().plusSeconds(seconds);
            Instant triggerTimeInstant = triggerTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            taskScheduler.schedule(task, triggerTimeInstant);
            writeLog(triggerTime, task);
            return triggerTime;
        } catch (Exception e) {
            log.error("Error scheduling task after {} seconds", seconds, e);
            return null;
        }
    }

    @Override
    public LocalDateTime runThisAfterMinutes(long minutes, Runnable task) {
        try{
            LocalDateTime triggerTime = LocalDateTime.now().plusMinutes(minutes);
            Instant triggerTimeInstant = triggerTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            taskScheduler.schedule(task, triggerTimeInstant);
            writeLog(triggerTime, task);
            return triggerTime;
        } catch (Exception e) {
            log.error("Error scheduling task after {} minutes", minutes, e);
            return null;
        }
    }

    @Override
    public LocalDateTime runThisAtBeforeSeconds(LocalDateTime stdTime, long seconds, Runnable task) {
        try {
            if(stdTime == null)
            {
                log.error("Standard time cannot be null!");
                return null;
            }
            LocalDateTime triggerTime = stdTime.minusSeconds(seconds);
            Instant triggerTimeInstant = triggerTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            Instant now = Instant.now();
            if(triggerTimeInstant.isBefore(now)){
                log.warn("Trigger time is in the past. Executing task immediately.");
                taskScheduler.schedule(task, now);
                writeLog(LocalDateTime.now(), task);
                return LocalDateTime.now();
            }
            else taskScheduler.schedule(task, triggerTimeInstant);
            writeLog(triggerTime, task);
            return triggerTime;
        } catch (Exception e) {
            log.error("Error scheduling task before {} seconds of {}", seconds, stdTime, e);
            return null;
        }
    }

    @Override
    public LocalDateTime runThisAtBeforeMinutes(LocalDateTime stdTime, long minutes, Runnable task) {
        try{
            if(stdTime == null)
            {
                log.error("Standard time cannot be null!");
                return null;
            }
            LocalDateTime triggerTime = stdTime.minusMinutes(minutes);
            Instant triggerTimeInstant = triggerTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            Instant now = Instant.now();
            if(triggerTimeInstant.isBefore(now)){
                log.warn("Trigger time is in the past. Executing task immediately.");
                taskScheduler.schedule(task, now);
                writeLog(LocalDateTime.now(), task);
                return LocalDateTime.now();
            }
            else taskScheduler.schedule(task, triggerTimeInstant);
            writeLog(triggerTime, task);
            return triggerTime;
        } catch (Exception e) {
            log.error("Error scheduling task before {} minutes of {}", minutes, stdTime, e);
            return null;
        }
    }

    private void writeLog(LocalDateTime scheduledTime, Runnable task)
    {
        log.info("{} 의 작업이 {} 시간대에 실행되도록 스케줄링되었습니다.", task.getClass().getSimpleName(), scheduledTime);
    }
}
