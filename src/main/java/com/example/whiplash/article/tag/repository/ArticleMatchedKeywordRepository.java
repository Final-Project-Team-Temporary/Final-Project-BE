package com.example.whiplash.article.tag.repository;

import com.example.whiplash.domain.entity.ArticleMatchedKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.zip.ZipFile;

public interface ArticleMatchedKeywordRepository extends JpaRepository<ArticleMatchedKeyword, Long> {
    boolean existsByArticleId(String articleId);

    List<ArticleMatchedKeyword> findByArticleId(String articleId);

    @Query("select distinct amk.articleId from ArticleMatchedKeyword amk")
    List<String> findDistinctArticleIds();

}
