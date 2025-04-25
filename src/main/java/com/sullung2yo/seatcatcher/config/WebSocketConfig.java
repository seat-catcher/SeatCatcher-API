package com.sullung2yo.seatcatcher.config;


import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.TokenException;
import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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

    public WebSocketConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.setMessageSizeLimit(64 * 1024); // 메세지 크기 제한 (64KB) -> 나중에 봐가면서 수정해야함
        registry.setTimeToFirstMessage(30000); // 클라이언트가 서버에 연결할 때, 첫 메세지를 보내기까지의 시간 제한 (30초)
        registry.setSendTimeLimit(60000); // 메세지 전송 시 Timeout 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/seatcatcher") // WebSocket Handshake 엔드포인트 (HTTP Request로 Websocket Handshake 진행하는 경로)
                .setAllowedOriginPatterns("*"); // CORS 허용 (모든 도메인 허용)
        // App에서만 사용할거니까 .withSocketJS() 사용 제거
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

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        /*
        Enable Simple Broker (브로커 경로 설정 - 구독 경로)
        - 내장 메세지 브로커를 활성화
        - 이것만으로 충분한 이유
        - 1. MVP 규모에서는 사용자가 많이 없어서, JVM 메모리 안에서 충분히 처리 가능
        - 2. 아래 한줄로 설정 끝 (간편함)
         */
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{10000, 10000})
                .setTaskScheduler(brokerTaskScheduler()); // Server->Client, Client->Server의 heartbeat 주기 설정

        // Heart-Beat : 클라이언트와 서버가 서로 연결이 살아있는지 확인하는 주기
        // 우리가 개발해야하는 모바일 서비스로 Wifi연결이나 셀룰러 연결이 불안정해서 웹소켓이 끊기는 경우가 있음
        // 따라서 Heart-Beat 주기를 설정해주면, 클라이언트와 서버가 서로 연결이 끊어졌을 때
        // 정해진 주기 안에 신호가 오지 않으면 연결이 끊어졌다고 판단하고, 클라이언트에서 재연결을 시도

        /*
        - 클라이언트가 서버로 메세지 보낼 때 사용할 접두사 -> /app (destination: /app/...)
        - STOMP 메세지 보낼 때 DESTINATION:/app/... 형식으로 경로 설정
        - 서버가 클라이언트에게 반환할때는 /topic/... 형식으로 메세지 전달한다. (클라이언트가 /topic/... 을 구독했으니까)
         */
        config.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public ThreadPoolTaskScheduler brokerTaskScheduler() {
        /*
         * Heartbeat을 위한 ThreadPoolTaskScheduler Bean 등록
         */
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(10); // 스레드 풀 사이즈 설정
        threadPoolTaskScheduler.setThreadNamePrefix("WebSocket-HeartBeat-");
        threadPoolTaskScheduler.initialize(); // 스레드 풀 초기화
        return threadPoolTaskScheduler;
    }
}
