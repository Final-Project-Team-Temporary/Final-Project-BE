package com.example.whiplash.article.repository;

import com.example.whiplash.article.document.SummarizedArticle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SummarizedArticleRepository extends MongoRepository<SummarizedArticle, String> {

    List<SummarizedArticle> findAllByPublishedAtAfter(LocalDateTime publishedAtAfter);
}
