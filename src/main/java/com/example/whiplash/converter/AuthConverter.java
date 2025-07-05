package com.example.whiplash.converter;

import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.AuthResponse;
import com.example.whiplash.user.dto.TokenResponseDTO;

public class AuthConverter {

    public static TokenResponseDTO toTokenResponseDTO(String accessToken, String refreshToken, UserStatus userStatus) {
        return TokenResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userStatus(userStatus)
                .build();
    }
}
