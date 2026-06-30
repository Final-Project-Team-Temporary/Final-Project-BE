package com.example.whiplash.kis.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KisTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType; // "Bearer"

    @JsonProperty("expires_in")
    private Long expiresIn; // 유효기간(초)

    @JsonProperty("access_token_token_expired")
    private String expiredAt; // 만료 일시
}
