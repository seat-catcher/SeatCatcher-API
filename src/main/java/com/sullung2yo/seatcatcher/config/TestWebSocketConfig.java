package com.sullung2yo.seatcatcher.config;

import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Slf4j
@Configuration
@Profile("test")
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class TestWebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    private final TokenProvider tokenProvider;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/seatcatcher") // WebSocket Handshake 엔드포인트 (HTTP Request로 Websocket Handshake 진행하는 경로)
                .setAllowedOriginPatterns("*"); // CORS 허용 (모든 도메인 허용)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // RabbitMQ 대신 내장된 SimpleBroker 사용
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
}
