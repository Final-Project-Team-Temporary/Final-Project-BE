package com.example.whiplash.article.summary.web.controller;

import java.time.LocalDateTime;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.summary.service.SummarizedArticleQueryService;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.global.util.SecurityContextUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/summarized-articles")
public class SummarizedArticleController {

	private final SummarizedArticleQueryService summarizedArticleQueryService;

	@GetMapping
	public ResponseEntity<ApiResponse<SummarizedArticleResponse>> getSummarizedArticles(
		@RequestParam String originalArticleId
	) {
		SummarizedArticleResponse response = summarizedArticleQueryService
			.getArticlesAndPublishEvent(SecurityContextUtils.getCurrentUserId(),
				originalArticleId,
				LocalDateTime.now()
			);

		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}
}
