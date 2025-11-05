package com.example.whiplash.user.web.dto.response;

import com.example.whiplash.user.domain.LoginStatus;
import com.example.whiplash.user.domain.UserStatus;
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
    private LoginStatus loginStatus;
}
