package com.example.whiplash.auth.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.example.whiplash.converter.AuthConverter;
import com.example.whiplash.converter.InvestorProfileConverter;
import com.example.whiplash.converter.UserConverter;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.user.domain.LoginStatus;
import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.domain.profile.InvestorProfile;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.LoginRequestDTO;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;
import com.example.whiplash.user.web.dto.response.KakaoUserInfoResponseDTO;
import com.example.whiplash.user.web.dto.response.TokenResponseDTO;
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
    private final InvestorProfileRepository investorProfileRepository;

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

        return AuthConverter.toTokenResponseDTO(tempToken, null, UserStatus.PENDING, LoginStatus.NEW_USER, newUser.getName());
    }

    @Transactional
    public TokenResponseDTO authenticateByKakao(String code) {
        String accessTokenFromKakao = kakaoAuthService.getAccessTokenFromKakao(code);
        KakaoUserInfoResponseDTO userInfo = kakaoAuthService.getKakaoUserInfo(accessTokenFromKakao);

        // 카카오 ID를 통해서 기존 유저 정보 조회
        Optional<User> user = userRepository.findByKakaoId(userInfo.getId());

        // 유저 정보가 이미 존재하는 경우 -> 바로 토큰 생성 후 반환
        if (user.isPresent()) {
            TokenResponseDTO tokenResponseDTO = loginByKakao(user.get());
            return tokenResponseDTO;
        }

        // 유저 정보가 없는 경우 -> 회원가입
        User kakaoUser = UserConverter.toKakaoUser(userInfo);

        userRepository.save(kakaoUser);

        // 회원가입의 경우 임시토큰 생성
        String tempToken = jwtTokenProvider.generateTempSocialToken(kakaoUser);
        return AuthConverter.toTokenResponseDTO(tempToken, null, UserStatus.PENDING, LoginStatus.NEW_USER, kakaoUser.getName());
    }

    @Transactional
    public TokenResponseDTO loginByKakao(User user) {

        if (user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        // 모든 사용자 타입에 대해 user.getId()를 사용
        String userId = String.valueOf(user.getId());

        user.updateLastLogin();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.singletonList(() -> user.getRole().name()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(refreshToken);

        return AuthConverter.toTokenResponseDTO(accessToken, refreshToken, UserStatus.ACTIVE, LoginStatus.EXISTING_USER, user.getName());
    }

    @Transactional
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        log.info("첫번째 조회 ----------------");

        if (user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        log.info("두번째 조회 ----------------");

        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new WhiplashException(ErrorStatus.INVALID_PASSWORD);
        }
        log.info("세번째 조회 ----------------");

        // 이메일 로그인도 user.getId()를 사용하여 일관성 유지
        Authentication authentication = new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), null,
                Collections.singleton(() -> user.getRole().name()));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(refreshToken);

        return AuthConverter.toTokenResponseDTO(accessToken, refreshToken, UserStatus.ACTIVE, LoginStatus.EXISTING_USER, user.getName());
    }

    @Transactional
    public void logout(String userId) {
        refreshTokenService.removeRefreshToken(userId);
    }

    @Transactional
    public TokenResponseDTO refreshToken(String refreshToken) {
        // validateRefreshToken에서 예외를 던지므로 별도 if 문 불필요
        refreshTokenService.validateRefreshToken(refreshToken);

        String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // userId로 사용자 조회 (일관성 유지)
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        // 사용자 상태 검증 추가
        if (user.getUserStatus() == UserStatus.PENDING || user.getUserStatus() == UserStatus.INACTIVE) {
            throw new WhiplashException(ErrorStatus.USER_NOT_ACTIVATED);
        }

        // user.getId()를 사용하여 일관성 유지
        Authentication authentication = new UsernamePasswordAuthenticationToken(String.valueOf(user.getId()), null,
                Collections.singleton(() -> user.getRole().name()));

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(newRefreshToken);

        return AuthConverter.toTokenResponseDTO(newAccessToken, newRefreshToken, UserStatus.ACTIVE, LoginStatus.EXISTING_USER, user.getName());
    }

    @Transactional
    public TokenResponseDTO completeRegistration(String userName, ProfileRegisterDTO request) {

        // userName은 이제 user.getId()로 통일됨
        User tempUser = userRepository.findById(Long.parseLong(userName))
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        InvestorProfile investorProfile = InvestorProfileConverter.toInvestorProfile(request, tempUser);

        investorProfileRepository.save(investorProfile);

        tempUser.activateUser();
        tempUser.updateRole(Role.USER);

        // user.getId()를 사용하여 일관성 유지
        Authentication authentication = new UsernamePasswordAuthenticationToken(String.valueOf(tempUser.getId()), null,
                Collections.singleton(() -> tempUser.getRole().name()));

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        refreshTokenService.saveRefreshToken(newRefreshToken);

        return AuthConverter.toTokenResponseDTO(newAccessToken, newRefreshToken, UserStatus.ACTIVE, LoginStatus.EXISTING_USER, tempUser.getName());

    }

}
