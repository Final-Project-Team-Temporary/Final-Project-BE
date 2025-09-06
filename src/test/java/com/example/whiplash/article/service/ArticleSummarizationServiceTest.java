package com.example.whiplash.article.service;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.article.domain.document.Article;
import com.example.whiplash.article.repository.ArticleRepository;
import com.example.whiplash.article.web.dto.request.ArticleSummarizationRequest;
import com.example.whiplash.article.web.dto.response.ArticleSummarizationResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.example.whiplash.article.domain.document.SummaryStatus.BEFORE_ENQUEUED;
import static org.assertj.core.api.Assertions.assertThat;

class ArticleSummarizationServiceTest extends IntegrationTestSupport {
    @Autowired
    private ArticleSummarizationService articleSummarizationService;
    @Autowired
    private ArticleRepository articleRepository;

    @AfterEach
    void tearDown() {
        articleRepository.deleteAll();
    }

    @DisplayName("크롤링이 완료된 기사들의 id 목록을 전달받으면 작업 큐에 등록한다.")
    @MethodSource("provideArticles")
    @ParameterizedTest
    public void should_add_article_id_to_queue_when_crawl_complete (List<Article> articles, List<String> articleIds, boolean result, int size) {
        // given
        articleRepository.saveAll(articles);
        ArticleSummarizationRequest request = createArticleSummarizationRequest(articleIds);

        // when
        ArticleSummarizationResponse response = articleSummarizationService.processArticleSummarizationRequest(request);

        // then
        assertThat(response).isNotNull()
                .extracting(ArticleSummarizationResponse::getProcessedCount, ArticleSummarizationResponse::isSuccess)
                .containsExactlyInAnyOrder(size, result);
    }

    private static ArticleSummarizationRequest createArticleSummarizationRequest(List<String> articleIds) {
        return ArticleSummarizationRequest.builder()
                .articleIds(articleIds)
                .timestamp(LocalDateTime.of(2025, 5, 1, 0, 0, 0))
                .build();
    }

    private static Stream<Arguments> provideArticles() {
        return Stream.of(
                Arguments.of(
                        List.of(createArticle("1", "title1")),
                        List.of("1"),
                        true,
                        1),
                Arguments.of(
                        List.of(createArticle("1", "title1"), createArticle("2", "title2")),
                        List.of("1", "2"),
                        true,
                        2)
        );
    }

    private static Article createArticle(String id,String title) {
        return Article.builder()
                .id(id)
                .title(title)
                .content("이것은 기사의 본문 내용입니다. 매우 중요한 정보가 담겨있습니다.")
                .url("https://example.com/news/12345")
                .press("테스트뉴스")
                .publishedAt(LocalDateTime.now())
                .summaryStatus(BEFORE_ENQUEUED)
                .build();
    }

    @DisplayName("존재하지 않는 기사 ID를 제공하면 작업큐에 등록할 때 실패한 ID 목록으로 포함된다")
    @Test
    public void should_contain_failed_id_when_provide_not_exist_article_id () {
        // given
        ArticleSummarizationRequest request = createArticleSummarizationRequest(List.of("1"));

        // when
        ArticleSummarizationResponse response = articleSummarizationService.processArticleSummarizationRequest(request);

        // then
        assertThat(response)
                .isNotNull()
                .extracting(r -> r.getFailedIds().size())
                .isEqualTo(1)
        ;
    }
}
