package com.example.whiplash.converter;

import com.example.whiplash.user.domain.LoginStatus;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.web.dto.response.TokenResponseDTO;

public class AuthConverter {

    public static TokenResponseDTO toTokenResponseDTO(String accessToken, String refreshToken, UserStatus userStatus, LoginStatus loginStatus) {
        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userStatus(userStatus)
                .loginStatus(loginStatus)
                .build();
    }
}
