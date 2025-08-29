package com.example.whiplash.article.document;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.article.repository.ArticleRepository;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
//@DataMongoTest
public class MongoCRUDTest extends IntegrationTestSupport {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private SummarizedArticleRepository summarizedArticleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void init() {
        // 1) 생성
        Article a = Article.builder()
                .title("테스트 제목")
                .press("jtbc")
                .publishedAt(LocalDateTime.now())
                .build();
        Article saved = articleRepository.save(a);

        // 1) 생성
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 1, 0, 0);
        SummarizedArticle sa = createSummarizedArticle(dateTime, dateTime);
        SummarizedArticle summary = summarizedArticleRepository.save(sa);
        assertThat(summary.getId()).isNotNull();

        log.info("summarizedArticle 아이디: {}", summary.getId());
    }

    @AfterEach
    void cleanUp() {
        //모든 Collection 초기화
        mongoTemplate.getDb().drop();
    }

    @Test
    @DisplayName("Article 기본 CRUD 동작 확인")
    void articleCrud() {
        // 1) 생성
        Article a = Article.builder()
                .title("테스트 제목2")
                .press("jtbc2")
                .publishedAt(LocalDateTime.now())
                .build();
        Article saved = articleRepository.save(a);


        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void find_shouldReturn1() {
        List<Article> all = articleRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
    }

    @Test
    void delete_shouldReturn0() {
        // 4) 삭제
        Article article = articleRepository.findAll().stream().findFirst().orElseThrow();

        articleRepository.deleteById(article.getId());
        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

    @Test
    @DisplayName("SummarizedArticle 기본 CRUD 동작 확인")
    void summarizedArticleCrud() {
        // 1) 생성
        LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 1, 0, 0);
        SummarizedArticle sa = createSummarizedArticle(dateTime, dateTime);
        SummarizedArticle saved = summarizedArticleRepository.save(sa);
        assertThat(saved.getId()).isNotNull();

        // 2) findAll
        List<SummarizedArticle> list = summarizedArticleRepository.findAll();
        assertThat(list.size()).isEqualTo(2);

        // 3) 삭제
        summarizedArticleRepository.delete(saved);
        assertThat(summarizedArticleRepository.findById(saved.getId())).isEmpty();
    }

    private static SummarizedArticle createSummarizedArticle(LocalDateTime summarizedAt, LocalDateTime publishedAt) {
        SummarizedArticle summary = SummarizedArticle.create("1",
                "하늘이 솟아오르다",
                Category.GOLD,
                "오늘 하늘이 솟아올랐다는 아주 놀라운 보고가 있다는데요. 맞나요 선생님.",
                SummaryLevel.SHORT,
                summarizedAt,
                publishedAt
        );
        return summary;
    }
}
