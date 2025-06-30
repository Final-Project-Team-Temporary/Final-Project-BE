package com.example.whiplash.auth.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.dto.AuthResponse;
import com.example.whiplash.user.dto.UserCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        AuthResponse authResponse = authService.joinUser(userCreateDTO);
        return ResponseEntity.ok(ApiResponse.onSuccess(authResponse));
    }
}
