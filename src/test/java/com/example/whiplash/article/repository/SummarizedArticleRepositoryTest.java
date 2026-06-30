package com.example.whiplash.article.repository;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import com.example.whiplash.article.summary.repository.SummarizedArticleRepository;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummarizedArticleRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SummarizedArticleRepository summarizedArticleRepository;

    @AfterEach
    void tearDown() {
        summarizedArticleRepository.deleteAll();
    }

    @DisplayName("오늘 완료된 Summary를 제외하고는 조회되지 않는다.")
    @Test
    public void should_return_0_summaries_when_after_time_criteria() {
        // given
        LocalDateTime summarizedAt1 = LocalDateTime.of(2025, 5, 1, 1, 0, 0);
        LocalDateTime publishedAt1 = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        SummarizedArticle summary1 = createSummarizedArticle(summarizedAt1, publishedAt1);

        LocalDateTime summarizedAt2 = LocalDateTime.of(2025, 5, 2, 1, 0, 0);
        LocalDateTime publishedAt2 = LocalDateTime.of(2025, 5, 2, 0, 0, 0);
        SummarizedArticle summary2 = createSummarizedArticle(summarizedAt2, publishedAt2);

        summarizedArticleRepository.save(summary1);
        summarizedArticleRepository.save(summary2);

        // when
        LocalDateTime criteria = LocalDateTime.of(2025, 5, 3, 0, 0, 0);
        List<SummarizedArticle> summarizedArticlesAfterCriteria = summarizedArticleRepository.findAllByPublishedAtGreaterThanEqual(criteria);

        // then
        assertThat(summarizedArticlesAfterCriteria).hasSize(0);
    }

    @DisplayName("오늘 완료된 Summary는 조회된다.")
    @Test
    public void should_hasSize_2_when_publishedAt_in_1day_from_criteria() {
        // given
        LocalDateTime summarizedAt1 = LocalDateTime.of(2025, 5, 1, 18, 0, 0);
        LocalDateTime publishedAt1 = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        SummarizedArticle summary1 = createSummarizedArticle(summarizedAt1, publishedAt1);

        LocalDateTime summarizedAt2 = LocalDateTime.of(2025, 5, 1, 18, 0, 0);
        LocalDateTime publishedAt2 = LocalDateTime.of(2025, 5, 1, 23, 59, 59);
        SummarizedArticle summary2 = createSummarizedArticle(summarizedAt2, publishedAt2);

        summarizedArticleRepository.save(summary1);
        summarizedArticleRepository.save(summary2);

        // when
        LocalDateTime criteria = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        List<SummarizedArticle> summarizedArticlesAfterCriteria = summarizedArticleRepository.findAllByPublishedAtGreaterThanEqual(criteria);

        // then
        assertThat(summarizedArticlesAfterCriteria).hasSize(2)
                .extracting(SummarizedArticle::getSummarizedAt)
                .containsExactlyInAnyOrder(summarizedAt1, summarizedAt2)
        ;
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