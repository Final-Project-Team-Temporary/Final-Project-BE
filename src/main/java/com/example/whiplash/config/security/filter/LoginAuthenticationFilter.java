package com.example.whiplash.config.security.filter;

import com.example.whiplash.user.web.dto.request.LoginRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * 커스텀 로그인 인증 필터
 * UsernamePasswordAuthenticationFilter를 상속받아 JSON 형식의 로그인 요청을 처리합니다.
 */
@Slf4j
public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper) {
        super(authenticationManager);
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        try {
            LoginRequestDTO loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDTO.class);

            log.info("로그인 시도: email={}", loginRequest.email());

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    );

            return this.getAuthenticationManager().authenticate(authenticationToken);

        } catch (IOException e) {
            log.error("로그인 요청 파싱 실패", e);
            throw new AuthenticationServiceException("로그인 요청을 파싱할 수 없습니다.", e);
        }
    }
}
