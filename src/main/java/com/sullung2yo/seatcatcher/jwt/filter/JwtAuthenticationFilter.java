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

    private Optional<String> extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return Optional.of(authHeader.substring(7));
        }
        return Optional.empty();
    }
}
