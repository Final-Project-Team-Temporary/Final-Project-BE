package com.example.whiplash.recommend.article.web.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.recommend.article.service.LoadArticleRecommendService;
import com.example.whiplash.recommend.article.web.dto.response.ArticleRecommendItemResponse;
import com.example.whiplash.recommend.article.web.dto.response.ArticleRecommendResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기사 추천 조회 API Controller
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/recommends/articles")
@RestController
public class LoadArticleRecommendController {

	private final LoadArticleRecommendService loadArticleRecommendService;

	/**
	 * 사용자별 추천 기사 목록 조회
	 * <p>
	 * 인증된 사용자의 추천 기사 목록을 페이징하여 조회합니다.
	 * 매일 12시에 스케줄러가 생성한 추천 결과를 반환합니다.
	 *
	 * @param page 페이지 번호 (0부터 시작, 기본값: 0)
	 * @param size 페이지 크기 (기본값: 10)
	 * @return 추천 기사 목록 (페이징)
	 */
	@GetMapping
	public ApiResponse<ArticleRecommendResponse> getRecommendedArticles(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		log.info("기사 추천 조회 요청 수신: page={}, size={}", page, size);

		Pageable pageable = PageRequest.of(page, size);
		Page<ArticleRecommendItemResponse> articles = loadArticleRecommendService.getRecommendedArticles(
			SecurityContextUtils.getCurrentUserId(),
			pageable
		);

		ArticleRecommendResponse response = ArticleRecommendResponse.from(articles);

		log.info("기사 추천 조회 완료: totalElements={}, currentPage={}, totalPages={}",
			response.totalElements(), response.currentPage(), response.totalPages());

		return ApiResponse.onSuccess(response);
	}
}
