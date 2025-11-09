package com.example.whiplash.keyword.article.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.keyword.article.domain.ArticleKeyword;
import com.example.whiplash.keyword.article.repository.ArticleKeywordRepository;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.user.repository.keyword.KeywordRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleKeywordService {
    private final ArticleKeywordRepository articleKeywordRepository;
    private final KeywordRepository keywordRepository;

    @Transactional
    public void saveArticleKeywords(String articleId, List<String> terms) {
        log.info("Processing article keywords for articleId={}, termCount={}", articleId, terms.size());

        for (String term : terms) {
            // 중복 검사: 이미 존재하는 ArticleKeyword인지 확인
            if (articleKeywordRepository.existsByArticleIdAndKeyword_Name(articleId, term)) {
                log.debug("ArticleKeyword already exists: articleId={}, term={}", articleId, term);
                continue;
            }

            // Keyword 조회 또는 생성
            Keyword keyword = keywordRepository.findByName(term)
                    .orElseGet(() -> {
                        log.info("Creating new Keyword: name={}", term);
                        return keywordRepository.save(Keyword.create(term));
                    });

            // ArticleKeyword 생성 및 저장
            ArticleKeyword articleKeyword = ArticleKeyword.create(articleId, keyword);
            articleKeywordRepository.save(articleKeyword);

            log.debug("Created ArticleKeyword: articleId={}, keywordId={}, keywordName={}",
                    articleId, keyword.getId(), keyword.getName());
        }

        log.info("Successfully processed article keywords for articleId={}", articleId);
    }
}
