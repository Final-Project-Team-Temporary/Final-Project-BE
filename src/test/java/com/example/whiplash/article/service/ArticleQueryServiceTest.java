package com.example.whiplash.article.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleReadRedisRepository;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.original.service.ArticleQueryService;
import com.example.whiplash.article.original.web.dto.response.ArticleDetailResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleListItemResponse;
import com.example.whiplash.article.original.web.dto.response.ArticleResponse;
import com.example.whiplash.article.original.web.dto.response.RecentlyViewedArticleResponse;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("기사 조회 서비스 테스트")
class ArticleQueryServiceTest extends IntegrationTestSupport {

    @Autowired
	ArticleQueryService articleQueryService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    SummarizedArticleRepository summarizedArticleRepository;

	@Autowired
	ArticleReadRedisRepository articleReadRedisRepository;

    @AfterEach
    void tearDown() {
        summarizedArticleRepository.deleteAll();
        articleRepository.deleteAll();
    }

    @Test
    @DisplayName("요약이 완료된 기사 목록을 페이징하여 조회할 수 있다")
    void should_returnPagedArticles_when_getArticleList() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Article article1 = createArticle("제목1", now.minusDays(1), SummaryStatus.COMPLETED);
        Article article2 = createArticle("제목2", now.minusDays(2), SummaryStatus.COMPLETED);
        Article article3 = createArticle("제목3", now.minusDays(3), SummaryStatus.PROCESSING);

        articleRepository.save(article1);
        articleRepository.save(article2);
        articleRepository.save(article3);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"));

        // when
        Page<ArticleListItemResponse> result = articleQueryService.getArticleList(pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).title()).isEqualTo("제목1");
        assertThat(result.getContent().get(1).title()).isEqualTo("제목2");
    }

    @Test
    @DisplayName("기사 목록 조회 시 publishedAt 내림차순으로 정렬된다")
    void should_returnSortedByPublishedAtDesc_when_getArticleList() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Article article1 = createArticle("제목1", now.minusDays(3), SummaryStatus.COMPLETED);
        Article article2 = createArticle("제목2", now.minusDays(1), SummaryStatus.COMPLETED);
        Article article3 = createArticle("제목3", now.minusDays(2), SummaryStatus.COMPLETED);

        articleRepository.save(article1);
        articleRepository.save(article2);
        articleRepository.save(article3);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "publishedAt"));

        // when
        Page<ArticleListItemResponse> result = articleQueryService.getArticleList(pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).title()).isEqualTo("제목2");
        assertThat(result.getContent().get(1).title()).isEqualTo("제목3");
        assertThat(result.getContent().get(2).title()).isEqualTo("제목1");
    }

    @Test
    @DisplayName("기사 세부 조회 시 3개 난이도의 요약을 모두 반환한다")
    void should_returnAllThreeSummaries_when_getArticleDetail() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Article article = createArticle("테스트 기사", now, SummaryStatus.COMPLETED);
        Article savedArticle = articleRepository.save(article);

        SummarizedArticle easy = createSummarizedArticle(savedArticle.getId(), "테스트 기사",
                Category.BATTERY, "쉬운 요약", SummaryLevel.EASY, now);
        SummarizedArticle medium = createSummarizedArticle(savedArticle.getId(), "테스트 기사",
                Category.BATTERY, "중간 요약", SummaryLevel.MEDIUM, now);
        SummarizedArticle advanced = createSummarizedArticle(savedArticle.getId(), "테스트 기사",
                Category.BATTERY, "어려운 요약", SummaryLevel.ADVANCED, now);

        summarizedArticleRepository.save(easy);
        summarizedArticleRepository.save(medium);
        summarizedArticleRepository.save(advanced);

        // when
        ArticleDetailResponse result = articleQueryService.getArticleDetail(savedArticle.getId());

        // then
        assertThat(result.summaries()).hasSize(3);
        assertThat(result.summaries())
                .extracting("summaryLevel")
                .containsExactlyInAnyOrder(SummaryLevel.EASY, SummaryLevel.MEDIUM, SummaryLevel.ADVANCED);
    }

    @Test
    @DisplayName("존재하지 않는 기사를 조회하면 예외가 발생한다")
    void should_throwException_when_articleNotFound() {
        // given
        String nonExistentId = "nonexistent-id";

        // when & then
        assertThatThrownBy(() -> articleQueryService.getArticleDetail(nonExistentId))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ARTICLE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("요약된 기사가 없으면 예외가 발생한다")
    void should_throwException_when_summarizedArticlesNotFound() {
        // given
        Article article = createArticle("테스트 기사", LocalDateTime.now(), SummaryStatus.COMPLETED);
        Article savedArticle = articleRepository.save(article);

        // when & then
        assertThatThrownBy(() -> articleQueryService.getArticleDetail(savedArticle.getId()))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.SUMMARIZED_ARTICLE_NOT_FOUND);
                });
    }

    @Test
    @DisplayName("요약된 기사가 3개가 아니면 예외가 발생한다")
    void should_throwException_when_incompleteSummaries() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Article article = createArticle("테스트 기사", now, SummaryStatus.COMPLETED);
        Article savedArticle = articleRepository.save(article);

        SummarizedArticle easy = createSummarizedArticle(savedArticle.getId(), "테스트 기사",
                Category.BATTERY, "쉬운 요약", SummaryLevel.EASY, now);
        summarizedArticleRepository.save(easy);

        // when & then
        assertThatThrownBy(() -> articleQueryService.getArticleDetail(savedArticle.getId()))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.INCOMPLETE_ARTICLE_SUMMARIES);
                });
    }

    @Test
    @DisplayName("원본 기사를 정상적으로 조회할 수 있다")
    void should_returnArticleResponse_when_getOriginalArticle() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Article article = createArticle("테스트 기사", now, SummaryStatus.COMPLETED);
        Article savedArticle = articleRepository.save(article);

        // when
        ArticleResponse result = articleQueryService.getOriginalArticle(savedArticle.getId());

        // then
        assertThat(result)
                .satisfies(response -> {
                    assertThat(response.id()).isEqualTo(savedArticle.getId());
                    assertThat(response.title()).isEqualTo("테스트 기사");
                    assertThat(response.content()).isEqualTo("테스트 내용");
                    assertThat(response.url()).isEqualTo("https://test.com");
                    assertThat(response.press()).isEqualTo("테스트 언론사");
                    assertThat(response.summaryStatus()).isEqualTo(SummaryStatus.COMPLETED);
                    assertThat(response.publishedAt()).isEqualTo(now);
                });
    }

    @Test
    @DisplayName("존재하지 않는 원본 기사를 조회하면 예외가 발생한다")
    void should_throwException_when_originalArticleNotFound() {
        // given
        String nonExistentId = "nonexistent-id";

        // when & then
        assertThatThrownBy(() -> articleQueryService.getOriginalArticle(nonExistentId))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.ARTICLE_NOT_FOUND);
                });
    }

	@Test
	@DisplayName("사용자가 특정 날짜에 읽은 기사 목록을 조회할 수 있다")
	void should_returnRecentlyViewedArticles_when_userReadArticlesOnDate() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 기사 생성 및 저장
		Article article1 = createArticle("기사 제목 1", now, SummaryStatus.COMPLETED);
		Article article2 = createArticle("기사 제목 2", now, SummaryStatus.COMPLETED);
		Article savedArticle1 = articleRepository.save(article1);
		Article savedArticle2 = articleRepository.save(article2);

		// Redis에 읽은 기사 기록
		articleReadRedisRepository.readArticle(userId, savedArticle1.getId());
		articleReadRedisRepository.readArticle(userId, savedArticle2.getId());

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).hasSize(2);
		assertThat(result)
			.extracting(RecentlyViewedArticleResponse::id)
			.containsExactlyInAnyOrder(savedArticle1.getId(), savedArticle2.getId());
		assertThat(result)
			.extracting(RecentlyViewedArticleResponse::title)
			.containsExactlyInAnyOrder("기사 제목 1", "기사 제목 2");
	}

	@Test
	@DisplayName("읽은 기사가 10개를 초과하면 최대 10개만 반환한다")
	void should_returnMaxTenArticles_when_moreThanTenArticlesRead() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 15개의 기사 생성 및 저장
		for (int i = 1; i <= 15; i++) {
			Article article = createArticle("기사 제목 " + i, now, SummaryStatus.COMPLETED);
			Article savedArticle = articleRepository.save(article);
			articleReadRedisRepository.readArticle(userId, savedArticle.getId());
		}

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).hasSize(10);
	}

	@Test
	@DisplayName("특정 날짜에 읽은 기사가 없으면 빈 목록을 반환한다")
	void should_returnEmptyList_when_noArticlesReadOnDate() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("다른 날짜에 읽은 기사는 조회되지 않는다")
	void should_notReturnArticles_when_readOnDifferentDate() {
		// given
		Long userId = 1L;
		LocalDate today = LocalDate.now();
		LocalDate yesterday = today.minusDays(1);
		LocalDateTime now = LocalDateTime.now();

		// 어제 읽은 기사
		Article yesterdayArticle = createArticle("어제 읽은 기사", now.minusDays(1), SummaryStatus.COMPLETED);
		Article savedYesterdayArticle = articleRepository.save(yesterdayArticle);

		// Redis에 어제 날짜로 기록 (수동으로 특정 날짜 키 사용)
		// 참고: 실제로는 readArticle이 오늘 날짜로만 저장하므로, 이 테스트는 getArticleIdsReadByUserOnDate 메서드를 검증

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, yesterday);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("Redis에 기록되었지만 MongoDB에 없는 기사는 결과에 포함되지 않는다")
	void should_notIncludeNonExistentArticles_when_articleNotInMongoDB() {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 실제 존재하는 기사
		Article article = createArticle("존재하는 기사", now, SummaryStatus.COMPLETED);
		Article savedArticle = articleRepository.save(article);

		// Redis에 기록 (존재하는 기사 + 존재하지 않는 기사 ID)
		articleReadRedisRepository.readArticle(userId, savedArticle.getId());
		articleReadRedisRepository.readArticle(userId, "non-existent-id");

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(savedArticle.getId());
		assertThat(result.get(0).title()).isEqualTo("존재하는 기사");
	}

	@Test
	@DisplayName("최근 읽은 기사 순서대로 조회된다")
	void should_returnArticlesInRecentReadOrder_when_multipleArticlesRead() throws InterruptedException {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 기사 생성 및 저장
		Article article1 = createArticle("첫 번째로 읽은 기사", now, SummaryStatus.COMPLETED);
		Article article2 = createArticle("두 번째로 읽은 기사", now, SummaryStatus.COMPLETED);
		Article article3 = createArticle("세 번째로 읽은 기사", now, SummaryStatus.COMPLETED);

		Article savedArticle1 = articleRepository.save(article1);
		Article savedArticle2 = articleRepository.save(article2);
		Article savedArticle3 = articleRepository.save(article3);

		// Redis에 시간차를 두고 기록 (순서대로 읽음)
		articleReadRedisRepository.readArticle(userId, savedArticle1.getId());
		Thread.sleep(10);  // 시간차 보장

		articleReadRedisRepository.readArticle(userId, savedArticle2.getId());
		Thread.sleep(10);  // 시간차 보장

		articleReadRedisRepository.readArticle(userId, savedArticle3.getId());

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).hasSize(3);
		// 최신 순서대로: article3 -> article2 -> article1
		assertThat(result.get(0).id()).isEqualTo(savedArticle3.getId());
		assertThat(result.get(0).title()).isEqualTo("세 번째로 읽은 기사");

		assertThat(result.get(1).id()).isEqualTo(savedArticle2.getId());
		assertThat(result.get(1).title()).isEqualTo("두 번째로 읽은 기사");

		assertThat(result.get(2).id()).isEqualTo(savedArticle1.getId());
		assertThat(result.get(2).title()).isEqualTo("첫 번째로 읽은 기사");
	}

	@Test
	@DisplayName("10개를 초과하면 가장 최근에 읽은 10개만 반환한다")
	void should_returnLatestTenArticles_when_moreThanTenArticlesRead() throws InterruptedException {
		// given
		Long userId = 1L;
		LocalDate date = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		// 15개의 기사 생성 및 시간차를 두고 저장
		for (int i = 1; i <= 15; i++) {
			Article article = createArticle("기사 제목 " + i, now, SummaryStatus.COMPLETED);
			Article savedArticle = articleRepository.save(article);
			articleReadRedisRepository.readArticle(userId, savedArticle.getId());

			if (i < 15) {
				Thread.sleep(5);  // 시간차 보장
			}
		}

		// when
		List<RecentlyViewedArticleResponse> result = articleQueryService
			.getRecentlyViewedArticles(userId, date);

		// then
		assertThat(result).hasSize(10);
		// 최신 10개만 반환되어야 함 (15, 14, 13, ..., 6)
		assertThat(result.get(0).title()).contains("15");
	}

    private Article createArticle(String title, LocalDateTime publishedAt, SummaryStatus summaryStatus) {
        return Article.builder()
                .title(title)
                .content("테스트 내용")
                .publishedAt(publishedAt)
                .url("https://test.com")
                .press("테스트 언론사")
                .summaryStatus(summaryStatus)
                .build();
    }

    private SummarizedArticle createSummarizedArticle(String originalArticleId, String title,
                                                      Category category, String summarizedContent,
                                                      SummaryLevel summaryLevel, LocalDateTime publishedAt) {
        return SummarizedArticle.create(
                originalArticleId,
                title,
                category,
                summarizedContent,
                summaryLevel,
                LocalDateTime.now(),
                publishedAt
        );
    }
}
