package com.sullung2yo.seatcatcher.jwt.filter;

import com.sullung2yo.seatcatcher.jwt.provider.TokenProvider;
import com.sullung2yo.seatcatcher.user.domain.User;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    /**
     * HTTP API에서 JWT 필터 만드는것 처럼,
     * WebSocket에서도 요청을 내부로 들여보내기 전에, 가로채서 검증하는게 필요하니까 이걸 구현해야함
     */

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // STOMP 헤더 정보 가져오기
        log.debug("WebSocket preSend 호출");
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class
        );

        // CONNECT (WebSocket 연결 요청) 인지 확인
        if (StompCommand.CONNECT == accessor.getCommand()) {
            log.debug("WebSocket CONNECT 요청");
            // STOMP 헤더에서 Authorization 헤더 가져오기
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            log.debug("Authorization Header: {}", authorizationHeader);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                // Bearer 떼기
                String token = authorizationHeader.substring(7);

                // JWT 토큰 검증
                String providerId = tokenProvider.getProviderIdFromToken(token);
                log.debug("providerId: {}", providerId);

                // 사용자 조회
                Optional<User> optionalUser = userRepository.findByProviderId(providerId);
                if (optionalUser.isPresent()) {
                    accessor.addNativeHeader("ProviderId", providerId);
                }
                else {
                    throw new IllegalArgumentException("올바르지 않은 인증 정보입니다.");
                }
            }
            else {
                throw new IllegalArgumentException("올바르지 않은 인증 정보입니다.");
            }
        }

        return message;
    }
}
