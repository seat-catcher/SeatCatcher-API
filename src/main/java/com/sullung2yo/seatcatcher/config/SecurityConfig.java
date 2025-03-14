package com.sullung2yo.seatcatcher.config;

import com.sullung2yo.seatcatcher.jwt.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * BCryptPasswordEncoder를 활용한 PasswordEncoder 빈을 생성합니다.
     *
     * 이 빈은 사용자 비밀번호 암호화를 위해 사용되며, 스프링 시큐리티 설정에서 인증 과정에 활용됩니다.
     *
     * @return 비밀번호 암호화를 위한 BCryptPasswordEncoder 구현체
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * JWT 인증 필터가 적용된 SecurityFilterChain을 생성합니다.
     *
     * <p>이 메서드는 CSRF와 CORS 보안을 비활성화하고, H2 콘솔의 iframe 사용을 허용합니다. 또한, 세션 관리를 STATELESS로 설정하여 REST API 환경에 적합하도록 구성합니다.
     * "/user/authenticate", "/health", "/h2-console/**" 엔드포인트는 인증 없이 접근할 수 있으며, 그 외의 모든 요청은 인증을 요구합니다.
     * 인증 실패 시 401, 인가 실패 시 403 상태 코드를 반환하며, JWT 토큰 검증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가합니다.
     *
     * @param http HTTP 보안 설정 구성을 위한 객체
     * @return 설정이 적용된 SecurityFilterChain 객체
     * @throws Exception 보안 설정 구성 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 설정을 비활성화
                .cors(AbstractHttpConfigurer::disable) // CORS 설정 비활성화 해서 모든 도메인에서 요청을 받을 수 있게 설정
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) //// H2 console은 iframe 사용
                .sessionManagement( // 세션 관리 설정 -> REST API 서버이기 때문에 세션을 사용하지 않으므로 STATELESS로 설정
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/user/authenticate").permitAll() // JWT 토큰 발급 API는 누구나 접근 가능
                        .requestMatchers("/health").permitAll() // 헬스체크 용 API 엔드포인트
                        .requestMatchers("/h2-console/**").permitAll() // h2-console 접근 허용
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 시 (토큰 없거나 잘못됨) -> 401
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 인가 실패(denyAll, 권한 부족) -> 403
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // JWT 토큰 검증 필터 추가
        return http.build();
    }
}
