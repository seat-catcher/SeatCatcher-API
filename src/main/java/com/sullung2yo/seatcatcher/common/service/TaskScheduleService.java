package com.sullung2yo.seatcatcher.common.service;

import java.time.LocalDateTime;

public interface TaskScheduleService {

    /*
        스케줄링이 필요한 곳에 TaskScheduleService 에 대한 의존성 주입이 필요합니다.
        예시 :
        private final TaskScheduleService scheduleService;

        모든 함수들의 호출 형태는 Runnable 을 고려하여 다음과 같이 하시면 됩니다.

        예시 :
        scheduleService.runThisAtBeforeMinutes(stdTime, 30, ()-> {
                // 이 곳에 예약을 걸어두고 싶은 명령어를 넣으면 됩니다. 예를 들면
                System.out.println("Hello world");
        });
    */


    // 이 인터페이스가 호출된 시점으로부터 seconds 초 후에 task 가 실행됩니다.
    LocalDateTime runThisAfterSeconds(long seconds, Runnable task);

    // 이 인터페이스가 호출된 시점으로부터 minutes 분 후에 task 가 실행됩니다.
    LocalDateTime runThisAfterMinutes(long minutes, Runnable task);

    // 입력받은 기준 시간 (ex 예상 도착 시간) 으로부터 seconds 초 전에 task 가 실행됩니다.
    LocalDateTime runThisAtBeforeSeconds(LocalDateTime stdTime, long seconds, Runnable task);

    // 입력받은 기준 시간 (ex 예상 도착 시간) 으로부터 minutes 분 전에 task 가 실행됩니다.
    LocalDateTime runThisAtBeforeMinutes(LocalDateTime stdTime, long minutes, Runnable task);

}
