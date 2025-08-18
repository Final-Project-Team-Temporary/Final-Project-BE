package com.example.whiplash.auth.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.dto.TokenResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KakaoTestController {

    private final AuthService authService;

    @GetMapping("/callback")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> authenticateByKakao(@RequestParam("code") String code) {
        TokenResponseDTO tokenResponseDTO = authService.authenticateByKakao(code);
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenResponseDTO));
    }
}
