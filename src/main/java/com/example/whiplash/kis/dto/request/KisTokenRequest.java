package com.example.whiplash.kis.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KisTokenRequest {

    @JsonProperty("grant_type")
    private String grantType; // "client_credentials" 고정

    @JsonProperty("appkey")
    private String appKey;

    @JsonProperty("appsecret")
    private String appSecret;
}
