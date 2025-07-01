package com.example.whiplash.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequestDTO {

    @NotBlank(message = "refreshToken이 반드시 필요합니다.")
    private String refreshToken;
}
