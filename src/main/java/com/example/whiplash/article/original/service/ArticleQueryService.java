package com.example.whiplash.article.original.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.converter.ArticleConverter;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleReadRedisRepository;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.original.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleResponse;
import com.example.whiplash.bookmark.entity.ArticleBookmark;
import com.example.whiplash.bookmark.repository.ArticleBookmarkRepository;
import com.example.whiplash.daily.learning.entity.LearningType;
import com.example.whiplash.daily.learning.event.LearningPerformedEvent;
import com.example.whiplash.global.event.DomainEventPublisher;
import com.example.whiplash.log.article.service.ArticleReadLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleQueryService {

	private final ArticleRepository articleRepository;
	private final SummarizedArticleRepository summarizedArticleRepository;
	private final ArticleBookmarkRepository bookmarkRepository;
	private final ArticleReadRedisRepository articleReadRedisRepository;
	private final ArticleReadLogService articleReadLogService;
	private final DomainEventPublisher eventPublisher;

	public Page<ArticleListItemResponse> getArticleList(Long userId, Pageable pageable) {
		Page<Article> articles = articleRepository.findBySummaryStatus(SummaryStatus.COMPLETED, pageable);

		// 2. 기사 ID 목록 추출
		List<String> articleIds = articles.getContent().stream()
			.map(Article::getId)
			.collect(Collectors.toList());

		// 3. ⭐ 사용자의 북마크 중 해당 기사들만 조회 (IN 쿼리 1회)
		List<ArticleBookmark> bookmarks = bookmarkRepository.findByUserIdAndArticleIdIn(
			userId,
			articleIds
		);

		// 4. ⭐ 북마크 맵 생성 (빠른 조회를 위해)
		Map<String, ArticleBookmark> bookmarkMap = bookmarks.stream()
			.collect(Collectors.toMap(
				ArticleBookmark::getArticleId,
				bookmark -> bookmark
			));

		// 5. DTO 변환 (북마크 여부 포함)
		Page<ArticleListItemResponse> response = articles.map(article -> {
			ArticleBookmark bookmark = bookmarkMap.get(article.getId());

			return ArticleListItemResponse.builder()
				.id(article.getId())
				.title(article.getTitle())
				.publishedAt(article.getPublishedAt())
				.isBookmarked(bookmark != null)  // ⭐ 북마크 여부
				.bookmarkId(bookmark != null ? bookmark.getId() : null)  // ⭐ 북마크 ID
				.build();
		});

		log.info("✅ 기사 목록 조회 완료: totalElements={}, bookmarkedCount={}",
			response.getTotalElements(), bookmarks.size());

		return response;
	}

	public ArticleResponse getArticleAndPublishEvent(Optional<Long> userIdOpt, String articleId, LocalDateTime now) {
		if (!userIdOpt.isEmpty()) {
			Long userId = userIdOpt.get();

			publishEventIfFirstReadArticle(articleId, userId, now);

			recordArticleReadLog(articleId, userId, now);
		}

		return getOriginalArticle(articleId);
	}

	private void publishEventIfFirstReadArticle(String articleId, Long userId, LocalDateTime now) {
		if (!articleReadRedisRepository.isRead(userId, articleId)) {
			// 처음 읽는 경우
			// 1. 학습 수행 이벤트 발행 (이벤트 기반 통신)
			eventPublisher.publish(
				new LearningPerformedEvent(userId, now, LearningType.ARTICLE)
			);

			// 2. Redis에 읽음 표시 (오늘 하루 유효)
			articleReadRedisRepository.readArticle(userId, articleId);

			log.info("✅ 사용자 {}가 기사 {}를 처음 읽음 - 이벤트 발행 완료", userId, articleId);
		}
	}

	private void recordArticleReadLog(String articleId, Long userId, LocalDateTime readAt) {
		// 3. DB에 영구 로그 저장
		Long id = articleReadLogService.saveArticleReadLog(userId, articleId, readAt);
		log.info("기사 읽기 로그 저장됨 아이디: {}", id);
	}

	public ArticleResponse getOriginalArticle(String articleId) {
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

		return ArticleConverter.toArticleResponse(article);
	}

	public ArticleDetailResponse getArticleDetail(String articleId) {
		// Article 존재 여부 확인
		Article article = articleRepository.findById(articleId)
			.orElseThrow(() -> new WhiplashException(ErrorStatus.ARTICLE_NOT_FOUND));

		// originalArticleId로 요약된 기사들 조회
		List<SummarizedArticle> summarizedArticles = summarizedArticleRepository
			.findAllByOriginalArticleId(articleId);

		// 요약된 기사가 없으면 예외
		if (summarizedArticles.isEmpty()) {
			throw new WhiplashException(ErrorStatus.SUMMARIZED_ARTICLE_NOT_FOUND);
		}

		// 3개의 난이도가 모두 있는지 검증 (선택적)
		if (summarizedArticles.size() != 3) {
			throw new WhiplashException(ErrorStatus.INCOMPLETE_ARTICLE_SUMMARIES);
		}

		return ArticleConverter.toArticleDetailResponse(summarizedArticles);
	}

	/**
	 * 디버깅용: 전체 기사의 상태별 분포를 조회
	 */
	public Map<SummaryStatus, Long> getArticleStatusDistribution() {
		List<Article> allArticles = articleRepository.findAll();
		return allArticles.stream()
			.collect(Collectors.groupingBy(Article::getSummaryStatus, Collectors.counting()));
	}

	public Page<ArticleListItemResponse> searchArticles(String keyword, Pageable pageable) {
		Page<Article> articles = articleRepository.searchByKeyword(keyword, pageable);
		return articles.map(ArticleConverter::toArticleListItemResponse);
	}
}
