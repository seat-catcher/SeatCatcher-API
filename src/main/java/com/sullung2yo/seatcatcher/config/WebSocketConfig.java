package com.sullung2yo.seatcatcher.config;


import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.TokenException;
import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
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
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;


@Slf4j
@Configuration
@EnableWebSocketMessageBroker // WebSocket Broker 활성화 (SpringBoot가 WebSocket을 지원하도록 활성화)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public WebSocketConfig(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(4 * 8192); // 메세지 크기 제한 (4KB)
        registry.setTimeToFirstMessage(30000); // 클라이언트가 서버에 연결할 때, 첫 메세지를 보내기까지의 시간 제한 (30초)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // WebSocket Handshake 엔드포인트 (HTTP Request로 Websocket Handshake 진행하는 경로)
                .setAllowedOriginPatterns("*"); // CORS 허용 (모든 도메인 허용)
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

                // CONNECT (WebSocket 연결 요청) 인지 확인
                assert accessor != null;
                log.debug("WebSocket {} 요청", accessor.getCommand());

                // STOMP 헤더에서 Authorization 헤더 가져오기
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
                    log.debug("Authorization Header: {}", authorizationHeader);
                    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                        // Bearer 떼기
                        String token = authorizationHeader.substring(7);

                        // 토큰 유효성 검사 및 인증 세션 생성
                        Authentication authentication = tokenProvider.getAuthenticationForWebSocket(token);
                        if (authentication != null) {
                            log.debug("WebSocket 인증 성공");
                            accessor.setUser(authentication);
                        } else {
                            throw new TokenException("올바르지 않은 인증 정보입니다.", ErrorCode.INVALID_TOKEN);
                        }
                    } else {
                        throw new TokenException("올바르지 않은 인증 정보입니다.", ErrorCode.INVALID_TOKEN);
                    }
                }
                return message;
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*
        Enable Simple Broker (브로커 경로 설정 - 구독 경로)
        - 내장 메세지 브로커를 활성화
         */
        config.enableSimpleBroker("/topic");

        /*
        - 클라이언트가 서버로 메세지 보낼 때 사용할 접두사 -> /app (destination: /app/...)
        - 클라이언트에서 /app/** 경로로 메세지를 보내면, 해당 메세지가 @MessageMapping 어노테이션이 붙은 메소드로 전달된다.
        - 예를 들어, /ws으로 WebSocket 연결된 상태에서, /pub/hello 경로로 메세지를 전달하면
        - @MessageMapping("/hello") 어노테이션이 붙은 메소드로 전달된다.
        - 서버는 내부 로직 처리 후에, /topic/... 이나 /queue/... 경로로 메세지를 전달한다.
         */
        config.setApplicationDestinationPrefixes("/app");
    }

}
