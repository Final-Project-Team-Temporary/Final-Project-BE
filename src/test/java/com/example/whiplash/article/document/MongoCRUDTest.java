package com.example.whiplash.article.document;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.MongoTestSupport;
import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.example.whiplash.article.original.domain.document.SummaryStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class MongoCRUDTest extends MongoTestSupport {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private SummarizedArticleRepository summarizedArticleRepository;

    @DisplayName("기사를 생성하고 저장한다.")
    @Test
    void testCreateArticle() {
        // given
        Article a = createArticle();

        // when
        Article saved = articleRepository.save(a);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(a.getTitle());
        assertThat(saved.getContent()).isEqualTo(a.getContent());
        assertThat(saved.getUrl()).isEqualTo(a.getUrl());
        assertThat(saved.getPress()).isEqualTo(a.getPress());
        assertThat(saved.getPublishedAt()).isEqualTo(a.getPublishedAt());
        assertThat(saved.getSummaryStatus()).isEqualTo(a.getSummaryStatus());
    }

    @DisplayName("저장된 기사를 조회한다.")
    @Test
    void should_hasSize_1_when_one_article_saved() {
        // given
        Article a = createArticle();
        articleRepository.save(a);

        // when
        List<Article> all = articleRepository.findAll();

        //then
        assertThat(all.size()).isEqualTo(1);
    }

    @DisplayName("기사를 삭제한다.")
    @Test
    void test_deleteArticle() {
        // given
        Article article = createArticle();
        articleRepository.save(article);

        // when
        articleRepository.deleteById(article.getId());

        // then
        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

    @Test
    @DisplayName("SummarizedArticle 기본 CRUD 동작 확인")
    void summarizedArticleCrud() {
        // 1) 생성
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 1, 0, 0);
        SummarizedArticle sa = createSummarizedArticle(dateTime, dateTime);
        SummarizedArticle saved = summarizedArticleRepository.save(sa);

        // 2) findAll
        List<SummarizedArticle> list = summarizedArticleRepository.findAll();
        assertThat(list.size()).isEqualTo(1);

        // 3) 삭제
        summarizedArticleRepository.delete(saved);
        assertThat(summarizedArticleRepository.findById(saved.getId())).isEmpty();
    }

    private static Article createArticle() {
        return Article.builder()
                .title("새로운 기사 제목")
                .content("이것은 기사의 본문 내용입니다. 매우 중요한 정보가 담겨있습니다.")
                .url("https://example.com/news/12345")
                .press("테스트뉴스")
                .publishedAt(LocalDateTime.now())
                .summaryStatus(BEFORE_ENQUEUED)
                .build();
    }

    @ParameterizedTest
    @MethodSource("provideSummaryStatusAndResult")
    @DisplayName("SummaryStatus별 Article 조회 쿼리 테스트")
    void findArticlesBySummaryStatus(List<Article> articles, SummaryStatus summaryStatus, Integer size) {
        // given
        articleRepository.saveAll(articles);

        // when
        List<Article> articleList = articleRepository.findBySummaryStatus(summaryStatus);

        // then
        assertThat(articleList.size()).isEqualTo(size);
    }

    static private Stream<Arguments> provideSummaryStatusAndResult() {
        List<Article> articles = Arrays.asList(
                createArticleWithStatus("기사1", BEFORE_ENQUEUED),
                createArticleWithStatus("기사2", ENQUEUED),
                createArticleWithStatus("기사3", COMPLETED),
                createArticleWithStatus("기사4", BEFORE_ENQUEUED),
                createArticleWithStatus("기사5", FAILED)
        );
        return Stream.of(
                Arguments.of(articles,
                        BEFORE_ENQUEUED,
                        2
                ),
                Arguments.of(articles,
                        ENQUEUED,
                        1
                ),
                Arguments.of(articles,
                        COMPLETED,
                        1
                ),
                Arguments.of(articles,
                        FAILED,
                        1
                ));
    }

    static private Article createArticleWithStatus(String title, SummaryStatus status) {
        return Article.builder()
                .title(title)
                .content("테스트용 기사 본문 내용")
                .url("https://test.com/news/" + title.hashCode())
                .press("테스트언론")
                .publishedAt(LocalDateTime.now())
                .summaryStatus(status)
                .build();
    }

    private static SummarizedArticle createSummarizedArticle(LocalDateTime summarizedAt, LocalDateTime publishedAt) {
        SummarizedArticle summary = SummarizedArticle.create("1",
                "하늘이 솟아오르다",
                Category.GOLD,
                "오늘 하늘이 솟아올랐다는 아주 놀라운 보고가 있다는데요. 맞나요 선생님.",
                SummaryLevel.EASY,
                summarizedAt,
                publishedAt
        );
        return summary;
    }
}
