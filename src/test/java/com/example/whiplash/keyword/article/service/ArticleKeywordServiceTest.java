package com.example.whiplash.keyword.article.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.keyword.article.domain.ArticleKeyword;
import com.example.whiplash.keyword.article.repository.ArticleKeywordRepository;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.user.repository.keyword.KeywordRepository;

@DisplayName("ArticleKeywordService 테스트")
class ArticleKeywordServiceTest extends IntegrationTestSupport {

    @Autowired
    ArticleKeywordService articleKeywordService;

    @Autowired
    ArticleKeywordRepository articleKeywordRepository;

    @Autowired
    KeywordRepository keywordRepository;

    @DisplayName("Keyword가 존재하지 않을 때 새로운 Keyword를 생성하고 ArticleKeyword를 생성한다")
    @Test
    void should_createKeywordAndArticleKeyword_when_keywordNotExists() {
        // given
        String articleId = "article123";
        List<String> terms = List.of("인공지능", "머신러닝");

        // when
        articleKeywordService.saveArticleKeywords(articleId, terms);

        // then
        List<ArticleKeyword> articleKeywords = articleKeywordRepository.findByArticleId(articleId);
        assertThat(articleKeywords).hasSize(2)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("인공지능", "머신러닝");

        List<Keyword> keywords = keywordRepository.findAll();
        assertThat(keywords)
                .extracting("name")
                .contains("인공지능", "머신러닝");
    }

    @DisplayName("Keyword가 이미 존재할 때 기존 Keyword를 사용하여 ArticleKeyword를 생성한다")
    @Test
    void should_useExistingKeyword_when_keywordExists() {
        // given
        String articleId = "article456";
        Keyword existingKeyword = keywordRepository.save(Keyword.create("블록체인"));
        List<String> terms = List.of("블록체인", "암호화폐");

        Long existingKeywordCount = keywordRepository.count();

        // when
        articleKeywordService.saveArticleKeywords(articleId, terms);

        // then
        List<ArticleKeyword> articleKeywords = articleKeywordRepository.findByArticleId(articleId);
        assertThat(articleKeywords).hasSize(2)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("블록체인", "암호화폐");

        // "블록체인"은 이미 존재했으므로 키워드는 1개만 추가되어야 함
        assertThat(keywordRepository.count()).isEqualTo(existingKeywordCount + 1);

        // 기존 키워드가 사용되었는지 확인
        ArticleKeyword blockchainArticleKeyword = articleKeywords.stream()
                .filter(ak -> "블록체인".equals(ak.getKeywordName()))
                .findFirst()
                .orElseThrow();
        assertThat(blockchainArticleKeyword.getKeyword().getId()).isEqualTo(existingKeyword.getId());
    }

    @DisplayName("동일한 articleId와 keyword로 중복 생성 시도 시 중복을 생성하지 않는다")
    @Test
    void should_notCreateDuplicate_when_articleKeywordAlreadyExists() {
        // given
        String articleId = "article789";
        Keyword keyword = keywordRepository.save(Keyword.create("클라우드"));
        articleKeywordRepository.save(ArticleKeyword.create(articleId, keyword));

        List<String> terms = List.of("클라우드", "데이터베이스");

        // when
        articleKeywordService.saveArticleKeywords(articleId, terms);

        // then
        List<ArticleKeyword> articleKeywords = articleKeywordRepository.findByArticleId(articleId);
        assertThat(articleKeywords).hasSize(2)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("클라우드", "데이터베이스");

        // "클라우드"는 중복되지 않아야 함
        long cloudCount = articleKeywords.stream()
                .filter(ak -> "클라우드".equals(ak.getKeywordName()))
                .count();
        assertThat(cloudCount).isEqualTo(1);
    }

    @DisplayName("여러 키워드를 한 번에 처리하여 ArticleKeyword를 생성한다")
    @Test
    void should_processMultipleKeywords_when_validTermsProvided() {
        // given
        String articleId = "article999";
        List<String> terms = List.of("자바", "스프링", "JPA", "레디스");

        // when
        articleKeywordService.saveArticleKeywords(articleId, terms);

        // then
        List<ArticleKeyword> articleKeywords = articleKeywordRepository.findByArticleId(articleId);
        assertThat(articleKeywords).hasSize(4)
                .extracting("keywordName")
                .containsExactlyInAnyOrder("자바", "스프링", "JPA", "레디스");

        // ArticleKeyword가 올바른 articleId를 가지고 있는지 확인
        assertThat(articleKeywords)
                .allSatisfy(ak -> assertThat(ak.getArticleId()).isEqualTo(articleId));
    }

    @DisplayName("ArticleKeyword 생성 시 keyword와 keywordName이 일치한다")
    @Test
    void should_haveMatchingKeywordName_when_articleKeywordCreated() {
        // given
        String articleId = "article555";
        List<String> terms = List.of("테스트");

        // when
        articleKeywordService.saveArticleKeywords(articleId, terms);

        // then
        List<ArticleKeyword> articleKeywords = articleKeywordRepository.findByArticleId(articleId);
        assertThat(articleKeywords).hasSize(1)
                .allSatisfy(ak -> {
                    assertThat(ak.getKeywordName()).isEqualTo(ak.getKeyword().getName());
                    assertThat(ak.getKeywordName()).isEqualTo("테스트");
                });
    }
}
