package com.example.whiplash.auth.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        TokenResponseDTO authResponse = authService.joinUser(userCreateDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        TokenResponseDTO tokenResponseDTO = authService.login(loginRequestDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenResponseDTO));
    }

    @PostMapping("/kakao-auth")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> authenticateByKakao(@RequestParam("code") String code) {
        TokenResponseDTO tokenResponseDTO = authService.authenticateByKakao(code);
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenResponseDTO));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userId = authentication.getName();

        authService.logout(userId);
        return ResponseEntity.ok(ApiResponse.onSuccess(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponseDTO>> refresh(@Valid @RequestBody TokenRefreshRequestDTO tokenRefreshRequestDTO) {
        TokenResponseDTO tokenResponseDTO = authService.refreshToken(tokenRefreshRequestDTO.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.onSuccess(tokenResponseDTO));
    }
}
