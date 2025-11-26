package com.example.whiplash.article.original.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.service.ArticleQueryService;
import com.example.whiplash.article.original.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleListResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleResponse;
import com.example.whiplash.article.original.web.dto.response.RecentlyViewedArticleResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.global.util.SecurityContextUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleLoadController {

	private final ArticleQueryService articleQueryService;

	@GetMapping("/summarized")
	public ResponseEntity<ApiResponse<ArticleListResponse>> getArticleList(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {

		Long userId = principal.getUserId();

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
		Page<ArticleListItemResponse> articles = articleQueryService.getArticleList(userId, pageable);
		ArticleListResponse response = ArticleListResponse.from(articles);

		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@GetMapping("/{articleId}")
	public ResponseEntity<ApiResponse<ArticleResponse>> getArticle(
		@PathVariable String articleId) {

		ArticleResponse article = articleQueryService.getArticleAndPublishEvent(
			SecurityContextUtils.getCurrentUserId()
			, articleId, LocalDateTime.now());

		return ResponseEntity.ok(ApiResponse.onSuccess(article));
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<ArticleListResponse>> searchArticles(
		@RequestParam String keyword,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
		Page<ArticleListItemResponse> articles = articleQueryService.searchArticles(keyword, pageable);
		ArticleListResponse response = ArticleListResponse.from(articles);

		return ResponseEntity.ok(ApiResponse.onSuccess(response));
	}

	@GetMapping("/recently-viewed")
	public ResponseEntity<ApiResponse<List<RecentlyViewedArticleResponse>>> getRecentlyViewedArticles(
		@AuthenticationPrincipal UserPrincipal principal,
		@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

		Long userId = principal.getUserId();

		List<RecentlyViewedArticleResponse> articles = articleQueryService.getRecentlyViewedArticles(userId, date);

		return ResponseEntity.ok(ApiResponse.onSuccess(articles));
	}

	/**
	 * 디버깅용: 전체 기사의 상태별 분포를 조회
	 */
	@GetMapping("/debug/status-distribution")
	public ResponseEntity<ApiResponse<Map<SummaryStatus, Long>>> getArticleStatusDistribution() {
		Map<SummaryStatus, Long> distribution = articleQueryService.getArticleStatusDistribution();
		return ResponseEntity.ok(ApiResponse.onSuccess(distribution));
	}
}
