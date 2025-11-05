package com.example.whiplash.article.summary.repository;

import com.example.whiplash.article.summary.domain.document.SummarizedArticle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SummarizedArticleRepository extends MongoRepository<SummarizedArticle, String> {

    List<SummarizedArticle> findAllByPublishedAtGreaterThanEqual(LocalDateTime publishedAtIsGreaterThan);

    List<SummarizedArticle> findAllByOriginalArticleId(String originalArticleId);
}
