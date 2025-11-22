package com.example.whiplash.article.tag.repository;

import com.example.whiplash.domain.entity.ArticleStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleStockRepository extends JpaRepository<ArticleStock, Long> {
    boolean existsByArticleId(String articleId);

    List<ArticleStock> findByArticleId(String articleId);
}
