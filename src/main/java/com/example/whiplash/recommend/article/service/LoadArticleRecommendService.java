package com.example.whiplash.recommend.article.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.article.repository.ArticleRecommendRedisRepository;
import com.example.whiplash.recommend.article.web.dto.response.ArticleRecommendItemResponse;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기사 추천 조회 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LoadArticleRecommendService {

	private final UserRepository userRepository;
	private final ArticleRecommendRedisRepository articleRecommendRedisRepository;

	/**
	 * 사용자별 추천 기사 목록 조회 (페이징)
	 *
	 * @param currentUserId 현재 인증된 사용자 ID
	 * @param pageable 페이징 정보
	 * @return 추천 기사 목록 (페이징)
	 */
	public Page<ArticleRecommendItemResponse> getRecommendedArticles(Optional<Long> currentUserId, Pageable pageable) {
		// 1. 사용자 인증 확인
		checkUserIsAuthenticated(currentUserId);

		// 2. 사용자 조회
		User user = userRepository.findById(currentUserId.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		// 3. Redis에서 추천 결과 조회
		ArticleRecommendResult recommendResult = articleRecommendRedisRepository.findByUser(user);

		if (recommendResult == null) {
			log.warn("추천 결과가 없습니다. userId={}", user.getId());
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		List<Article> articles = recommendResult.getArticles();

		// 4. 페이징 처리
		int start = (int)pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), articles.size());

		// 범위 초과 체크
		if (start > articles.size()) {
			return new PageImpl<>(Collections.emptyList(), pageable, articles.size());
		}

		List<Article> pagedArticles = articles.subList(start, end);

		// 5. DTO 변환
		List<ArticleRecommendItemResponse> content = pagedArticles.stream()
			.map(ArticleRecommendItemResponse::from)
			.toList();

		log.info("추천 기사 조회 완료: userId={}, page={}, size={}, totalElements={}",
			user.getId(), pageable.getPageNumber(), content.size(), articles.size());

		return new PageImpl<>(content, pageable, articles.size());
	}

	/**
	 * 사용자 인증 확인
	 */
	private static void checkUserIsAuthenticated(Optional<Long> currentUserId) {
		if (currentUserId.isEmpty()) {
			throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
		}
	}
}
