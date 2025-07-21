package com.sullung2yo.seatcatcher.common.websocket;

import com.sullung2yo.seatcatcher.common.domain.TokenType;
import com.sullung2yo.seatcatcher.common.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.domain.user.enums.ProfileImageNum;
import com.sullung2yo.seatcatcher.domain.auth.enums.Provider;
import com.sullung2yo.seatcatcher.domain.user.entity.User;
import com.sullung2yo.seatcatcher.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@AutoConfigureMockMvc
public class WebSocketConnectionTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProviderImpl jwtTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp()
    {
        // AccessToken 생성을 위해 더미 사용자 생성
        User user = User.builder()
                .provider(Provider.APPLE)
                .providerId(String.valueOf(System.currentTimeMillis()))
                .name("testUser")
                .credit(123L)
                .profileImageNum(ProfileImageNum.IMAGE_1)
                .build();
        userRepository.save(user);
        userRepository.flush();

        // Access 토큰 생성
        accessToken = jwtTokenProvider.createToken(user.getProviderId(), null, TokenType.ACCESS);
    }

    // 얼떨결에 WebSocket 테스트용 코드가 돼버렸는데, 해당 테스트가 정말 의미가 있을지는 두고 봐야 함.
    @Test
    void webSocketConnectionTest() throws InterruptedException, ExecutionException, TimeoutException
    {
        assertDoesNotThrow(()->{
            StompSession stompSession;

            WebSocketStompClient stompClient;

            stompClient = new WebSocketStompClient(new StandardWebSocketClient());
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            // 연길 시 사용하는 JWT 설정
            WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
            webSocketHttpHeaders.add("Authorization", "Bearer " + accessToken);

            // STOMP 사용시 사용하는 JWT 설정
            StompHeaders stompHeaders = new StompHeaders();
            stompHeaders.add("Authorization", "Bearer " + accessToken);


            // STOMP 연결
            CompletableFuture<StompSession> futureSession = stompClient.connectAsync(
                    "ws://localhost:" + port + "/seatcatcher"
                    ,webSocketHttpHeaders, stompHeaders
                    ,new StompSessionHandlerAdapter(){}
            );

            stompSession = futureSession.get(5, TimeUnit.SECONDS);
        });
    }

    /*
    @Test
    void publishPathHistoryEvent_shouldReceiveWebSocketMessageWhenMessageIsPublishedToRabbitMQ() throws Exception
    {
        // given
        PathHistoryResponse.PathHistoryInfoResponse mockData = new PathHistoryResponse.PathHistoryInfoResponse();
        mockData.setId(1L);

        BlockingQueue<PathHistoryResponse.PathHistoryInfoResponse> blockingQueue = new LinkedBlockingDeque<>();
        // 블로킹 큐가 뭔진 모르겠는데 "스레드 안전 큐" 라고 해서 Websocket 메시지가 도착하면 여따 임시로 저장하려고 선언한댔음.

        stompSession.subscribe("/topic/path-histories." + mockData.getId(), new StompFrameHandler() {
            @Override
            public @NonNull Type getPayloadType(@NonNull StompHeaders headers)
            {
                return PathHistoryResponse.PathHistoryInfoResponse.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, Object payload)
            {
                boolean isSuccess = blockingQueue.offer((PathHistoryResponse.PathHistoryInfoResponse)payload);
                if(!isSuccess)
                {
                    log.warn("PublishEventTest :: Failed to enqueue websocket message in test!");
                }
            }
        });

        rabbitTemplate.convertAndSend(exchangeName, "path-histories." + mockData.getId(), mockData);

        PathHistoryResponse.PathHistoryInfoResponse received = blockingQueue.poll(5, TimeUnit.SECONDS);

        assertNotNull(received);
        assertEquals(mockData.getId(), received.getId());
    }
*/


}
