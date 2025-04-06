package com.sullung2yo.seatcatcher.subway_station.utility;

import com.sullung2yo.seatcatcher.subway_station.domain.SubwayLine;
import com.sullung2yo.seatcatcher.subway_station.repository.SubwayLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SubwayLineInitializer implements CommandLineRunner {

    private final SubwayLineRepository subwayLineRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1 ~ 8호선 노선 정보를 SubwayLine 테이블에 실행 시 저장 또는 업데이트한다 (로컬, 개발 환경)
        // 이미 존재하면 건너뛰고, 없으면 새로 만든다.
        for (int lineNum = 1; lineNum <= 9; lineNum++) {
            String lineName = String.valueOf(lineNum);

            boolean exists = subwayLineRepository.existsByLineName(lineName);
            if (!exists) {
                SubwayLine newLine = SubwayLine.builder()
                        .lineName(lineName)
                        .build();
                subwayLineRepository.save(newLine);
            }
        }

    }
}
