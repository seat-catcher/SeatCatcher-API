package com.sullung2yo.seatcatcher.train.service;

import com.sullung2yo.seatcatcher.train.domain.*;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static reactor.core.publisher.Mono.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TrainServiceTest {

    private TrainService service;

    @Mock
    private TrainRepository trainRepository;

    @Autowired
    private TrainHelperService helperService;

    @BeforeEach
    void setUp() {
        service = new TrainServiceImpl(trainRepository, helperService);
    }

    @Test
    @DisplayName("열차 코드와 차량 코드로 그룹을 찾거나 생성하는 기능 테스트")
    void testFindOrCreateByTrainCodeAndCarCode() {
        // given
        String trainCode = "9999";
        String carCode = "9999";

        Train mockTrain = new Train();
        mockTrain.setTrainCode(trainCode);
        mockTrain.setCarCode(carCode);

        List<Train> trains = List.of(mockTrain);

        // when
        System.out.println("service : " + service);
        List<Train> result = service.findOrCreateByTrainCodeAndCarCode(trainCode, carCode);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(trainCode, result.get(0).getTrainCode());
        Assertions.assertEquals(carCode, result.get(0).getCarCode());

        // when
        result = service.findOrCreateByTrainCodeAndCarCode("99999", "99999"); // DB에는 없는걸로 질의

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("99999", result.get(0).getTrainCode());
        Assertions.assertEquals("99999", result.get(0).getCarCode());
    }
}
