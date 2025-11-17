package com.example.whiplash.config.security.handler;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.auth.service.RefreshTokenService;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.response.TokenResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 로그인 성공 시 JWT 토큰을 생성하고 응답하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("로그인 성공: user={}", authentication.getName());

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        // userId를 principal로 하는 새로운 Authentication 생성
        Authentication userIdAuthentication = new UsernamePasswordAuthenticationToken(
                String.valueOf(user.getId()),
                null,
                Collections.singleton(() -> user.getRole().name())
        );

        // JWT 토큰 생성 (userId가 subject로 들어감)
        String accessToken = jwtTokenProvider.generateAccessToken(userIdAuthentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userIdAuthentication);

        // Refresh Token 저장
        refreshTokenService.saveRefreshToken(refreshToken);

        // 응답 데이터 생성
        TokenResponseDTO tokenResponse = TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userStatus(UserStatus.ACTIVE)
                .build();

        ApiResponse<TokenResponseDTO> apiResponse = ApiResponse.onSuccess(tokenResponse);

        // JSON 응답 작성
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
