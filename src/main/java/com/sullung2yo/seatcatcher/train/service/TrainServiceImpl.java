package com.sullung2yo.seatcatcher.train.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sullung2yo.seatcatcher.train.dto.response.LiveTrainLocationResponse;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Slf4j
@Service
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    @Value("${api.seoul.live.key}")
    private String liveApiKey;

    public TrainServiceImpl (
            TrainRepository trainRepository,
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.trainRepository = trainRepository;
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<List<LiveTrainLocationResponse>> fetchLiveTrainLocation(String lineNumber) {
        String LIVE_TRAIN_LOCATION_API_URL = "http://swopenAPI.seoul.go.kr/api/subway/" + liveApiKey + "/json/realtimePosition/0/100/";
        log.debug("실시간 열차 위치 정보 API 호출");
        return webClient.get()
                .uri(LIVE_TRAIN_LOCATION_API_URL + lineNumber)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> Mono.just(parseResponse(response)))
                .doOnError(e -> log.error("API 호출 중 오류 발생", e))
                .doFinally(signal -> log.debug("실시간 열차 위치 정보 API 호출 완료")) // Reactive 방식에서는 try-catch를 사용하지 않고, 이런식으로 처리
                .onErrorReturn(Collections.emptyList());
    }

    @Override
    @Transactional
    public void saveLiveTrainLocation() {
    }

    private List<LiveTrainLocationResponse> parseResponse(String liveTrainLocation) {
        if (liveTrainLocation == null) {
            log.warn("API 응답이 null입니다.");
            return Collections.emptyList(); // API 응답이 null인 경우 빈 리스트 반환
        }

        try {
            JsonNode rootNode = objectMapper.readTree(liveTrainLocation);
            JsonNode errorMessage = rootNode.path("errorMessage");
            String code = errorMessage.path("code").asText();

            if (!code.equals("INFO-000") && !code.equals("INFO-200")) {
                log.warn("API 응답 코드가 유효하지 않음: {}", code);
                return Collections.emptyList(); // 유효하지 않은 코드인 경우 빈 리스트 반환
            }

            JsonNode positionList = rootNode.path("realtimePositionList");
            List<LiveTrainLocationResponse> responseList = new ArrayList<>();
            for (JsonNode node : positionList) {
                responseList.add(objectMapper.treeToValue(node, LiveTrainLocationResponse.class));
            }

            return responseList;
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage(), e);
            return Collections.emptyList(); // JSON 파싱 오류인 경우 빈 리스트 반환
        }
    }
}
