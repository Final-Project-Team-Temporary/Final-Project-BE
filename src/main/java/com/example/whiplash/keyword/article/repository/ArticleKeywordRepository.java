package com.example.whiplash.keyword.article.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.whiplash.keyword.article.domain.ArticleKeyword;

public interface ArticleKeywordRepository extends JpaRepository<ArticleKeyword, Long> {
    List<ArticleKeyword> findByArticleId(String articleId);

    boolean existsByArticleIdAndKeyword_Name(String articleId, String keywordName);
}
