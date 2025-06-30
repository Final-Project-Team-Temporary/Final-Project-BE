package com.example.whiplash.converter;

import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.AuthResponse;

public class AuthConverter {

    public static AuthResponse toAuthResponse(String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .userStatus(UserStatus.PENDING)
                .build();
    }
}
