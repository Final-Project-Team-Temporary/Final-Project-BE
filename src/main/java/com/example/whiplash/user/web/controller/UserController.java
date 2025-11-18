package com.example.whiplash.user.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/profile-setup")
    public ResponseEntity<ApiResponse<?>> profileSetup(@Valid @RequestBody ProfileRegisterDTO profileRegisterDTO) {

        Optional<Long> optionalUserId = SecurityContextUtils.getCurrentUserId();

        userService.registerProfile(profileRegisterDTO, optionalUserId);
        return ResponseEntity.ok(ApiResponse.onCreated(null));
    }

    @GetMapping("/profile")
    public ApiResponse<?> getProfile(@AuthenticationPrincipal UserPrincipal principal) {

        Long userId = principal.getUserId();

        return ApiResponse.onSuccess(userService.getUserProfile(userId));
    }
}
