package com.example.whiplash.config.security.jwt;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }


    @DisplayName("인증 객체로부터 엑세스 토큰을 발급받을 수 있다.")
    @Test
    void generateAccessToken(){

        //given
        Authentication auth = new UsernamePasswordAuthenticationToken("username",
                "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        //when
        String accessToken = jwtTokenProvider.generateAccessToken(auth);

        //then
        Assertions.assertThat(accessToken).isNotNull();
        Assertions.assertThat(accessToken).isNotEmpty();
        Assertions.assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
    }


    @DisplayName("토큰으로부터 유저의 Id 를 추출할 수 있다.")
    @Test
    void getUserPrincipalFromToken(){

        //given
        Authentication auth = new UsernamePasswordAuthenticationToken("username",
                "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String accessToken = jwtTokenProvider.generateAccessToken(auth);

        //when
        String userId = jwtTokenProvider.getUserIdFromToken(accessToken);

        //then
        Assertions.assertThat(userId).isNotNull();
        Assertions.assertThat(userId).isNotEmpty();
        Assertions.assertThat(userId).isEqualTo("username");
    }


    @DisplayName("유효하지 않은 토큰은 검증과정에서 False를 반환한다.")
    @Test
    void validateToken_InvalidToken(){

        //given
        String invalidToken = "invalidToken";

        //when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        //then
        Assertions.assertThat(isValid).isFalse();
    }

}