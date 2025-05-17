package com.sullung2yo.seatcatcher.config;

import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // STOMP 헤더 정보 가져오기
                log.debug("WebSocket preSend 호출");
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class
                );
                log.debug("WebSocket preSend accessor: {}", accessor);

                if (accessor == null) { // accessor가 null이 아닐 때만 진행
                    return message;
                }
                log.debug("WebSocket {} 요청", accessor.getCommand());

                // STOMP 헤더에서 Authorization 헤더 가져오기
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        // Bearer 떼기
                        String token = authorizationHeader.substring(7);

                        // 토큰 유효성 검사 및 인증 세션 생성
                        Authentication authentication = tokenProvider.getAuthenticationForWebSocket(token);
                        if (authentication != null) {
                            log.debug("WebSocket 인증 성공");
                            accessor.setUser(authentication);
                        } else {
                            // MessagingException 사용하면 Spring이 알아서 자동으로 Error 처리해줌
                            throw new MessagingException("올바르지 않은 인증 정보입니다.");
                        }
                    } else {
                        throw new MessagingException("헤더에 JWT 토큰 정보가 없습니다.");
                    }
                }
                return message;
            }
        });
    }
}
