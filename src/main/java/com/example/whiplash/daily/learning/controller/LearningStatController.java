package com.example.whiplash.daily.learning.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.daily.learning.dto.LearningStreakResponse;
import com.example.whiplash.daily.learning.service.DailyLearningStatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/learning")
public class LearningStatController {

	private final DailyLearningStatService dailyLearningStatService;

	/**
	 * 사용자의 연속 학습일 조회
	 */
	@GetMapping("/streak")
	public ApiResponse<LearningStreakResponse> getLearningStreak(@AuthenticationPrincipal UserPrincipal principal) {
		LearningStreakResponse response = dailyLearningStatService.getLearningStreak(principal.getUserId());
		
		return ApiResponse.onSuccess(response);
	}
	
}
