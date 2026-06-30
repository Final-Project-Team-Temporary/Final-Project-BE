package com.example.whiplash.kis.service;

import com.example.whiplash.kis.dto.request.KisTokenRequest;
import com.example.whiplash.kis.dto.response.KisTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "kis-auth", url = "${kis.api.url}")
public interface KisAuthClient {

    /**
     * 접근 토큰 발급 (P)
     * URL: /oauth2/tokenP
     */
    @PostMapping(value = "/oauth2/tokenP", consumes = MediaType.APPLICATION_JSON_VALUE)
    KisTokenResponse issueToken(@RequestBody KisTokenRequest request);
}
