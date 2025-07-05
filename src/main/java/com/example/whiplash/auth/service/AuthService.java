package com.example.whiplash.auth.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.example.whiplash.converter.AuthConverter;
import com.example.whiplash.converter.UserConverter;
import com.example.whiplash.user.User;
import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.*;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final KakaoAuthService kakaoAuthService;

    @Transactional
    public TokenResponseDTO joinUser(UserCreateDTO userCreateDTO) {
        Optional<User> user = userRepository.findByEmail(userCreateDTO.getEmail());

        if (user.isPresent()) {
            throw new WhiplashException(ErrorStatus.DUPLICATE_EMAIL);
        }

        User newUser = UserConverter.toUser(userCreateDTO);

        newUser.encodePassword(passwordEncoder.encode(userCreateDTO.getPassword()));

        userRepository.save(newUser);

        String tempToken = jwtTokenProvider.generateTempToken(newUser);

        return AuthConverter.toTokenResponseDTO(tempToken, null, UserStatus.PENDING);
    }

    @Transactional
    public TokenResponseDTO authenticateByKakao(String code){
        String accessTokenFromKakao = kakaoAuthService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDTO userInfo = kakaoAuthService.getKakaoUserInfo(accessTokenFromKakao);

        Optional<User> user = userRepository.findByKakaoId(userInfo.getId());

        if (user.isPresent()) {
            TokenResponseDTO tokenResponseDTO = loginByKakao(user.get());
            return tokenResponseDTO;
        }

        User kakaoUser = UserConverter.toKakaoUser(userInfo);

        userRepository.save(kakaoUser);

        String tempToken = jwtTokenProvider.generateTempSocialToken(kakaoUser);
        return AuthConverter.toTokenResponseDTO(tempToken, null, UserStatus.PENDING);
    }

    @Transactional
    public TokenResponseDTO loginByKakao(User user) {

        if(user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        String userId = user.getSocialProvider().name() + "_" +  user.getKakaoId();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(() -> user.getRole().name()));


        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(refreshToken);

        return AuthConverter.toTokenResponseDTO(accessToken, refreshToken, UserStatus.ACTIVE);
    }

    @Transactional
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        log.info("첫번째 조회 ----------------");

        if(user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        log.info("두번째 조회 ----------------");

        if(!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new WhiplashException(ErrorStatus.INVALID_PASSWORD);
        }
        log.info("세번째 조회 ----------------");

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                Collections.singleton(()-> user.getRole().name()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(refreshToken);

        return AuthConverter.toTokenResponseDTO(accessToken, refreshToken, UserStatus.ACTIVE);
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenService.removeRefreshToken(userId);
    }

    @Transactional
    public TokenResponseDTO refreshToken(String refreshToken) {
        // validateRefreshToken에서 예외를 던지므로 별도 if 문 불필요
        refreshTokenService.validateRefreshToken(refreshToken);

        String email = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        // 사용자 상태 검증 추가
        if(user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null,
                Collections.singleton(() -> user.getRole().name()));

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(newRefreshToken);

        return AuthConverter.toTokenResponseDTO(newAccessToken, newRefreshToken, UserStatus.ACTIVE);
    }

}
