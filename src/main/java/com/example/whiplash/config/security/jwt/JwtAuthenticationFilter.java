package com.example.whiplash.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = jwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String userIdFromToken = jwtTokenProvider.getUserIdFromToken(token);
            String authoritiesFromToken = jwtTokenProvider.getAuthoritiesFromToken(token);

            User principal = new User(userIdFromToken, "", Collections.singletonList(new SimpleGrantedAuthority(authoritiesFromToken)));

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // 공개 API 경로들
        String[] excludePaths = {
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh",
                "/api/public/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/health"
        };

        return Arrays.stream(excludePaths)
                .anyMatch(excludePath -> {
                    AntPathMatcher pathMatcher = new AntPathMatcher();
                    return pathMatcher.match(excludePath, path);
                });
    }

}
