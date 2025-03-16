package com.sullung2yo.seatcatcher.jwt.filter;

import com.sullung2yo.seatcatcher.jwt.provider.JwtTokenProviderImpl;
import com.sullung2yo.seatcatcher.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProviderImpl jwtTokenProvider;
    private final UserRepository userRepository;


    /**
     * HTTP 요청에서 JWT를 추출하여 토큰을 검증한 후, 유효한 경우 사용자 인증 정보를 SecurityContext에 설정한다.
     * 요청 헤더의 "Authorization" 항목에서 JWT를 추출하고 토큰을 파싱하여 클레임으로부터 사용자 이메일을 확인한다.
     * 이메일이 존재하고 현재 SecurityContext에 인증 정보가 없는 경우, 데이터베이스에서 사용자를 조회한 후 역할 정보를 포함한
     * 인증 토큰을 생성하여 SecurityContext에 설정한다. 토큰 검증 중 JwtException이 발생하면 SecurityContext를 초기화한다.
     * 마지막으로, 다음 필터 처리를 위해 요청과 응답을 필터 체인에 전달한다.
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 다음 필터 처리를 위한 체인
     * @throws ServletException 서블릿 관련 예외 발생 시
     * @throws IOException 입출력 관련 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. HTTP Header에서 Authorization 항목을 검사한다.
        Optional<String> token = extractToken(request);

        if (token.isPresent() && StringUtils.hasText(token.get())) {
            try {
                // JWT 파싱
                Claims claims = Jwts.parser()
                        .verifyWith(jwtTokenProvider.getSecretKey())
                        .build()
                        .parseSignedClaims(token.get())
                        .getPayload();

                String email = claims.getSubject(); // Email로 subject를 만들었으니까 이렇게 가져와준다.

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 사용자가 존재하는지 확인
                    userRepository.findByEmail(email).ifPresent(user -> {
                        // 사용자가 존재하면 사용자의 권한을 가져와서 Authentication 객체를 만들어서 SecurityContext에 넣어준다.
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                email, null, Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString())));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                }
            } catch (JwtException e) {
                // JWT 토큰이 유효하지 않은 경우
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 "Authorization" 헤더에서 JWT 토큰을 추출합니다.
     *
     * 요청 헤더에 "Bearer " 접두어가 포함되어 있을 경우, 접두어 이후의 토큰 문자열을 반환하며,
     * 그렇지 않으면 빈 Optional을 반환합니다.
     *
     * @param request JWT 토큰이 포함될 수 있는 HTTP 요청
     * @return 토큰 문자열이 포함된 Optional, 유효한 토큰이 없으면 empty Optional
     */
    private Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}
