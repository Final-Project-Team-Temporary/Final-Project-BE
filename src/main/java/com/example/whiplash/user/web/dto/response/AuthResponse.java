package com.example.whiplash.user.web.dto.response;

import com.example.whiplash.user.domain.UserStatus;
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
