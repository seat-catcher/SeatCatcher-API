package com.sullung2yo.seatcatcher.common.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TaskScheduleServiceTest {

    @Autowired
    private TaskScheduleService taskScheduleService;

    private boolean boolValue1;
    private boolean boolValue2;

    @BeforeEach
    void setUp(){
        boolValue1 = false;
        boolValue2 = false;
    }

    @Test
    public void taskScheduleServiceTest() {
        // when
        taskScheduleService.runThisAfterSeconds(1, () -> boolValue1 = true);

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(()->
            assertTrue(boolValue1, "runThisAfter 스케줄이 제대로 동작하지 않습니다.")
        );

        // when
        // 현재 시각의 3초 후의 미래를 기준으로 했을 때 2초 전에 예약했던 작업이 잘 수행되는지 확인. ( 즉 1초 후 )
        taskScheduleService.runThisAtBeforeSeconds(LocalDateTime.now().plusSeconds(3), 2, ()-> boolValue2 = true);

        // then
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(()->
            assertTrue(boolValue2, "runThisAtBefore 스케줄이 제대로 동작하지 않습니다.")
        );
    }
}
