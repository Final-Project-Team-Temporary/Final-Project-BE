package com.example.whiplash.config.security.handler;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 로그인 실패 시 에러 응답을 처리하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        log.error("로그인 실패: {}", exception.getMessage());

        ErrorStatus errorStatus = determineErrorStatus(exception);

        ApiResponse<?> apiResponse = ApiResponse.onFailure(errorStatus);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    private ErrorStatus determineErrorStatus(AuthenticationException exception) {
        if (exception instanceof BadCredentialsException) {
            return ErrorStatus.INVALID_PASSWORD;
        } else if (exception instanceof UsernameNotFoundException ||
                   exception instanceof InternalAuthenticationServiceException) {
            return ErrorStatus.USER_NOT_FOUND;
        } else if (exception instanceof DisabledException) {
            return ErrorStatus.USER_NOT_ACTIVATED;
        } else {
            return ErrorStatus.UNAUTHORIZED;
        }
    }
}
