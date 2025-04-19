package com.sullung2yo.seatcatcher.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.FcmException;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.user.dto.request.FcmMessage;
import com.sullung2yo.seatcatcher.user.dto.request.FcmRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final ObjectMapper objectMapper;

    @Value("${fcm.url}")
    private String FCM_API_URL;

    @Value("${fcm.file_path}")
    private String FIREBASE_CONFIG_PATH;

    @Value("${fcm.google_api}")
    private String GOOGLE_API_URI;


    @Override
    public void sendMessageTo(FcmRequest request) throws IOException {
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(FCM_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(makeMessage(request.getTargetToken(), request.getTitle(), request.getBody()))
                .header(AUTHORIZATION,"Bearer " + getAccessToken())
                .header(ACCEPT, "application/json; UTF-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (fcmRequest, fcmResponse) -> {
                    throw new FcmException("Firebase 메시지 전송 실패:"+fcmResponse.getBody(), ErrorCode.INVALID_REQUEST_URI);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (fcmRequest, fcmResponse) -> {
                    throw new FcmException("FCM 요청 URI가 잘못되었습니다", ErrorCode.FIREBASE_SERVER_ERROR);
                })
                .toBodilessEntity();

    }

    private String makeMessage(String targetToken, String title, String body) throws com.fasterxml.jackson.core.JsonProcessingException { // JsonParseException, JsonProcessingException
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }

    // Firebase Admin SDK의 비공개 키를 참조하여 Bearer 토큰을 발급 받는다.
    private String getAccessToken() throws IOException {

        try {
            final GoogleCredentials googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(FIREBASE_CONFIG_PATH).getInputStream())
                    .createScoped(List.of(GOOGLE_API_URI));

            googleCredentials.refreshIfExpired();
            log.info("access token: {}", googleCredentials.getAccessToken());
            return googleCredentials.getAccessToken().getTokenValue();

        } catch (IOException e) {
            throw new TokenException("firebase access token을 발급받지 못했습니다.",ErrorCode.INVALID_TOKEN);
        }
    }

}
