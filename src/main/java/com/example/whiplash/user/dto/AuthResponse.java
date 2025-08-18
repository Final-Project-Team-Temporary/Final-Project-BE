package com.example.whiplash.user.dto;

import com.example.whiplash.user.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class AuthResponse {
    private String accessToken;
    private UserStatus userStatus;
}
