package com.sullung2yo.seatcatcher.user.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.FcmException;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.common.exception.UserException;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.dto.request.FcmMessage;
import com.sullung2yo.seatcatcher.user.dto.request.FcmMessageWithData;
import com.sullung2yo.seatcatcher.user.dto.request.FcmRequest;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${fcm.url}")
    private String FCM_API_URL;

    @Value("${fcm.file_path}")
    private String FIREBASE_CONFIG_PATH;

    @Value("${fcm.google_api}")
    private String GOOGLE_API_URI;


    @Override
    public void sendMessageTo(FcmRequest.Notification request) throws IOException {
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(FCM_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(makeMessage(request.getTargetToken(), request.getTitle(), request.getBody()))
                .header(AUTHORIZATION,"Bearer " + getAccessToken())
                .header(ACCEPT, "application/json; UTF-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (fcmRequest, fcmResponse) -> {
                    String error = new String(fcmResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String errorMessage = objectMapper.readTree(error).toPrettyString();

                    throw new FcmException("Firebase 메시지 전송 실패 : " + errorMessage, ErrorCode.INVALID_REQUEST_URI);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (fcmRequest, fcmResponse) -> {
                    throw new FcmException("FCM 요청 URI가 잘못되었습니다", ErrorCode.FIREBASE_SERVER_ERROR);
                })
                .toBodilessEntity();
    }

    @Override
    public void sendMessageTo(FcmRequest.NotificationAndData request) throws IOException {
        RestClient restClient = RestClient.create();
        restClient.post()
                .uri(FCM_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(makeMessage(request.getTargetToken(), request.getTitle(), request.getBody(), request.getData()))
                .header(AUTHORIZATION,"Bearer " + getAccessToken())
                .header(ACCEPT, "application/json; UTF-8")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (fcmRequest, fcmResponse) -> {
                    String error = new String(fcmResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);

                    ObjectMapper objectMapper = new ObjectMapper();
                    String errorMessage = objectMapper.readTree(error).toPrettyString();

                    throw new FcmException("Firebase 메시지 전송 실패 : " + errorMessage, ErrorCode.INVALID_REQUEST_URI);
                })
                .onStatus(HttpStatusCode::is5xxServerError, (fcmRequest, fcmResponse) -> {
                    throw new FcmException("FCM 요청 URI가 잘못되었습니다", ErrorCode.FIREBASE_SERVER_ERROR);
                })
                .toBodilessEntity();
    }

    @Override
    public void saveToken(FcmRequest.Token request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String providerId = authentication.getName();
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new UserException("해당 id를 가진 사용자를 찾을 수 없습니다. providerId : " + providerId, ErrorCode.USER_NOT_FOUND));

        user.setFcmToken(request.getToken());
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

    private String makeMessage(String targetToken, String title, String body, Object data) throws com.fasterxml.jackson.core.JsonProcessingException {
        String dataPayload = objectMapper.writeValueAsString(data);
        objectMapper.readTree(dataPayload); // JSON 형태가 아니면 여기서 Exception 발생.

        FcmMessageWithData fcmMessage = FcmMessageWithData.builder()
                .message(FcmMessageWithData.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessageWithData.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        )
                        .data(
                                Map.of(
                                        "payload", dataPayload
                                )
                        )
                        .build()).validateOnly(false).build();
        return objectMapper.writeValueAsString(fcmMessage);
    }

    // Firebase Admin SDK의 비공개 키를 통한 accessToken발급
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
