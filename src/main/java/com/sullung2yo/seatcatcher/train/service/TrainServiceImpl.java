package com.sullung2yo.seatcatcher.train.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import com.sullung2yo.seatcatcher.train.dto.response.LiveTrainLocationResponse;
import com.sullung2yo.seatcatcher.train.repository.TrainRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
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
    public List<LiveTrainLocationResponse> fetchLiveTrainLocation(String lineNumber) {
        try {
            String LIVE_TRAIN_LOCATION_API_URL = "http://swopenAPI.seoul.go.kr/api/subway/" + liveApiKey + "/json/realtimePosition/0/100/";
            log.debug("실시간 열차 위치 정보 API 호출");
            Mono<String> liveTrainLocationResponse = webClient.get()
                    .uri(LIVE_TRAIN_LOCATION_API_URL + lineNumber)
                    .retrieve()
                    .bodyToMono(String.class);

            String liveTrainLocation = liveTrainLocationResponse.block();
            if (liveTrainLocation == null) {
                log.error("API 응답이 null입니다.");
                throw new RuntimeException("API 응답이 null입니다.");
            }

            JsonObject parsedJson = JsonParser.parseString(liveTrainLocation).getAsJsonObject();
            log.debug("결과: {}", parsedJson);
            JsonObject errorMessage = parsedJson.getAsJsonObject("errorMessage");
            if (errorMessage.isEmpty()) { // errorMessage : 에러가 아니라 에러인지 아닌지 상태를 보여주는 응답
                log.error("API 결과가 올바르지 않습니다.");
            }

            if (
                    errorMessage.get("code").getAsString().equals("INFO-000") ||
                    errorMessage.get("code").getAsString().equals("INFO-200")
            ) {
                JsonNode rootNode = objectMapper.readTree(liveTrainLocation);
                JsonNode positionList = rootNode.path("realtimePositionList");

                List<LiveTrainLocationResponse> responseList = new ArrayList<>();
                for (JsonNode node : positionList) {
                    responseList.add(objectMapper.treeToValue(node, LiveTrainLocationResponse.class));
                }
                return responseList;
            }

        } catch (RuntimeException e) {
            log.error("실시간 열차 위치 정보 API 호출 작업 중, 에러 발생 : {}", e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            log.debug("실시간 열차 위치 정보 API 호출 완료");
        }
        return null;
    }

    @Override
    @Transactional
    public void saveLiveTrainLocation() {
    }


}
