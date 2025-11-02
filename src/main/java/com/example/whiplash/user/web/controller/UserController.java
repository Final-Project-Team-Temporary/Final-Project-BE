package com.example.whiplash.user.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/profile-setup")
    public ResponseEntity<ApiResponse<?>> profileSetup(@Valid @RequestBody ProfileRegisterDTO profileRegisterDTO) {

        Optional<String> optionalEmail = SecurityContextUtils.getCurrentUserEmail();

        userService.registerProfile(profileRegisterDTO, optionalEmail);
        return ResponseEntity.ok(ApiResponse.onCreated(null));
    }
}
