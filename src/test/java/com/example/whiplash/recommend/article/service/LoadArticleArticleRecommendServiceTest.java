package com.example.whiplash.recommend.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.recommend.article.application.service.LoadArticleRecommendService;
import com.example.whiplash.recommend.article.domain.ArticleRecommendResult;
import com.example.whiplash.recommend.article.repository.ArticleRecommendRedisRepository;
import com.example.whiplash.recommend.article.web.dto.response.ArticleRecommendItemResponse;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;

@DisplayName("기사 추천 조회 서비스 테스트")
class LoadArticleArticleRecommendServiceTest extends IntegrationTestSupport {

	@Autowired
	private UserRepository userRepository;

	@MockBean
	private ArticleRecommendRedisRepository articleRecommendRedisRepository;

	@Autowired
	private LoadArticleRecommendService loadArticleRecommendService;

	@Test
	@DisplayName("인증되지 않은 사용자가 추천을 요청하면 예외를 던져야 한다")
	void should_throwUnauthorizedException_when_userNotAuthenticated() {
		// given
		Optional<Long> emptyUserId = Optional.empty();
		Pageable pageable = PageRequest.of(0, 10);

		// when & then
		assertThatThrownBy(() -> loadArticleRecommendService.getRecommendedArticles(emptyUserId, pageable))
			.isInstanceOf(WhiplashException.class)
			.satisfies(exception -> {
				WhiplashException ex = (WhiplashException)exception;
				assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.UNAUTHORIZED);
			});
	}

	@Test
	@DisplayName("존재하지 않는 사용자가 추천을 요청하면 예외를 던져야 한다")
	void should_throwUserNotFoundException_when_userNotExists() {
		// given
		Long nonExistentUserId = 99999L;
		Pageable pageable = PageRequest.of(0, 10);

		// when & then
		assertThatThrownBy(
			() -> loadArticleRecommendService.getRecommendedArticles(Optional.of(nonExistentUserId), pageable))
			.isInstanceOf(WhiplashException.class)
			.satisfies(exception -> {
				WhiplashException ex = (WhiplashException)exception;
				assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND);
			});
	}

	@Test
	@DisplayName("추천 결과가 없으면 빈 페이지를 반환해야 한다")
	void should_returnEmptyPage_when_noRecommendations() {
		// given
		User user = createUser("test@example.com");
		userRepository.save(user);

		given(articleRecommendRedisRepository.findByUser(user)).willReturn(null);

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ArticleRecommendItemResponse> result = loadArticleRecommendService.getRecommendedArticles(
			Optional.of(user.getId()),
			pageable
		);

		// then
		assertThat(result)
			.satisfies(page -> {
				assertThat(page.getContent()).isEmpty();
				assertThat(page.getTotalElements()).isZero();
				assertThat(page.getTotalPages()).isZero();
			});
	}

	@Test
	@DisplayName("페이징된 추천 기사를 올바르게 조회해야 한다")
	void should_returnPagedRecommendations_when_recommendationsExist() {
		// given
		User user = createUser("test@example.com");
		userRepository.save(user);

		List<Article> articles = createMockArticles(15);
		ArticleRecommendResult recommendResult = new ArticleRecommendResult(
			articles,
			user,
			LocalDateTime.now()
		);

		given(articleRecommendRedisRepository.findByUser(user)).willReturn(recommendResult);

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ArticleRecommendItemResponse> result = loadArticleRecommendService.getRecommendedArticles(
			Optional.of(user.getId()),
			pageable
		);

		// then
		assertThat(result)
			.satisfies(page -> {
				assertThat(page.getContent()).hasSize(10);
				assertThat(page.getTotalElements()).isEqualTo(15);
				assertThat(page.getTotalPages()).isEqualTo(2);
				assertThat(page.hasNext()).isTrue();
				assertThat(page.hasPrevious()).isFalse();
			});
	}

	@Test
	@DisplayName("두 번째 페이지를 올바르게 조회해야 한다")
	void should_returnSecondPage_when_requestingSecondPage() {
		// given
		User user = createUser("test@example.com");
		userRepository.save(user);

		List<Article> articles = createMockArticles(15);
		ArticleRecommendResult recommendResult = new ArticleRecommendResult(
			articles,
			user,
			LocalDateTime.now()
		);

		given(articleRecommendRedisRepository.findByUser(user)).willReturn(recommendResult);

		Pageable pageable = PageRequest.of(1, 10);

		// when
		Page<ArticleRecommendItemResponse> result = loadArticleRecommendService.getRecommendedArticles(
			Optional.of(user.getId()),
			pageable
		);

		// then
		assertThat(result)
			.satisfies(page -> {
				assertThat(page.getContent()).hasSize(5);
				assertThat(page.getTotalElements()).isEqualTo(15);
				assertThat(page.getTotalPages()).isEqualTo(2);
				assertThat(page.hasNext()).isFalse();
				assertThat(page.hasPrevious()).isTrue();
			});
	}

	@Test
	@DisplayName("범위를 초과한 페이지를 요청하면 빈 페이지를 반환해야 한다")
	void should_returnEmptyPage_when_pageOutOfRange() {
		// given
		User user = createUser("test@example.com");
		userRepository.save(user);

		List<Article> articles = createMockArticles(5);
		ArticleRecommendResult recommendResult = new ArticleRecommendResult(
			articles,
			user,
			LocalDateTime.now()
		);

		given(articleRecommendRedisRepository.findByUser(user)).willReturn(recommendResult);

		Pageable pageable = PageRequest.of(5, 10);

		// when
		Page<ArticleRecommendItemResponse> result = loadArticleRecommendService.getRecommendedArticles(
			Optional.of(user.getId()),
			pageable
		);

		// then
		assertThat(result)
			.satisfies(page -> {
				assertThat(page.getContent()).isEmpty();
				assertThat(page.getTotalElements()).isEqualTo(5);
			});
	}

	@Test
	@DisplayName("DTO 변환이 올바르게 수행되어야 한다")
	void should_convertToDTO_correctly() {
		// given
		User user = createUser("test@example.com");
		userRepository.save(user);

		Article article = createArticle("1", "테스트 기사", "테스트 언론사");
		List<Article> articles = List.of(article);
		ArticleRecommendResult recommendResult = new ArticleRecommendResult(
			articles,
			user,
			LocalDateTime.now()
		);

		given(articleRecommendRedisRepository.findByUser(user)).willReturn(recommendResult);

		Pageable pageable = PageRequest.of(0, 10);

		// when
		Page<ArticleRecommendItemResponse> result = loadArticleRecommendService.getRecommendedArticles(
			Optional.of(user.getId()),
			pageable
		);

		// then
		assertThat(result.getContent()).hasSize(1);
		ArticleRecommendItemResponse dto = result.getContent().get(0);
		assertThat(dto)
			.satisfies(d -> {
				assertThat(d.id()).isEqualTo(article.getId());
				assertThat(d.title()).isEqualTo(article.getTitle());
				assertThat(d.press()).isEqualTo(article.getPress());
				assertThat(d.url()).isEqualTo(article.getUrl());
				assertThat(d.publishedAt()).isEqualTo(article.getPublishedAt());
			});
	}

	// Helper methods
	private User createUser(String email) {
		return User.builder()
			.email(email)
			.userName("테스트 사용자")
			.build();
	}

	private List<Article> createMockArticles(int count) {
		List<Article> articles = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			articles.add(createArticle(
				String.valueOf(i),
				"테스트 기사 " + i,
				"테스트 언론사 " + i
			));
		}
		return articles;
	}

	private Article createArticle(String id, String title, String press) {
		return Article.builder()
			.id(id)
			.title(title)
			.press(press)
			.url("https://example.com/article/" + id)
			.content("테스트 내용")
			.publishedAt(LocalDateTime.now())
			.summaryStatus(SummaryStatus.NOT_STARTED)
			.build();
	}
}
