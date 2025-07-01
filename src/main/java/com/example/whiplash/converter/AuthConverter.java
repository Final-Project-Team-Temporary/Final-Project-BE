package com.example.whiplash.converter;

import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.AuthResponse;
import com.example.whiplash.user.dto.TokenResponseDTO;

public class AuthConverter {

    public static AuthResponse toAuthResponse(String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .userStatus(UserStatus.PENDING)
                .build();
    }

    public static TokenResponseDTO toTokenResponseDTO(String accessToken, String refreshToken) {
        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
