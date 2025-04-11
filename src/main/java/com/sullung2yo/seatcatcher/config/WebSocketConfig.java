package com.sullung2yo.seatcatcher.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // WebSocket Broker 활성화 (SpringBoot가 WebSocket을 지원하도록 활성화)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket 기본 엔드포인트 (HTTP Request로 Handshake 진행하는 경로)
                .setAllowedOrigins("*") // CORS 설정
                .withSockJS(); // WebSocket 연결을 위한 엔드포인트 등록
        // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 기술을 지원하는 메서드인데 이게 지금 필요한가?
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 endpoint prefix
        // 이 prefix로 시작하면, Message broker가 작업을 처리한다.
        config.enableSimpleBroker("/queue");

        // 클라이언트에서 서버로 전송 시 붙이는 prefix
        config.setApplicationDestinationPrefixes("/app");
    }
}
