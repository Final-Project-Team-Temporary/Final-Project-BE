package com.example.whiplash.article.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.article.summary.service.SummarizedArticleQueryService;
import com.example.whiplash.article.summary.web.dto.response.SummarizedArticleResponse;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("요약 기사 조회 서비스 테스트")
class SummarizedArticleQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    SummarizedArticleQueryService summarizedArticleQueryService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    SummarizedArticleRepository summarizedArticleRepository;

    @AfterEach
    void tearDown() {
        summarizedArticleRepository.deleteAll();
        articleRepository.deleteAll();
    }

    @Test
    @DisplayName("요약된 기사 조회 시 3개 난이도의 요약을 모두 반환한다")
    void should_returnAllThreeSummaries_when_getSummarizedArticles() {
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
        SummarizedArticleResponse result = summarizedArticleQueryService
                .getSummarizedArticles(savedArticle.getId());

        // then
        assertThat(result.summaries()).hasSize(3);
        assertThat(result.summaries())
                .extracting("summaryLevel")
                .containsExactlyInAnyOrder(SummaryLevel.EASY, SummaryLevel.MEDIUM, SummaryLevel.ADVANCED);
    }

    @Test
    @DisplayName("존재하지 않는 원본 기사 ID로 조회하면 예외가 발생한다")
    void should_throwException_when_originalArticleNotFound() {
        // given
        String nonExistentId = "nonexistent-id";

        // when & then
        assertThatThrownBy(() -> summarizedArticleQueryService.getSummarizedArticles(nonExistentId))
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
        assertThatThrownBy(() -> summarizedArticleQueryService.getSummarizedArticles(savedArticle.getId()))
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
        assertThatThrownBy(() -> summarizedArticleQueryService.getSummarizedArticles(savedArticle.getId()))
                .isInstanceOf(WhiplashException.class)
                .satisfies(exception -> {
                    WhiplashException ex = (WhiplashException) exception;
                    assertThat(ex.getErrorStatus()).isEqualTo(ErrorStatus.INCOMPLETE_ARTICLE_SUMMARIES);
                });
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
