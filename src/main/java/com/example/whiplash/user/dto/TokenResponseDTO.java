package com.example.whiplash.user.dto;

import com.example.whiplash.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class TokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private UserStatus userStatus;
}
