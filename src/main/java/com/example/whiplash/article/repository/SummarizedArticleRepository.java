package com.example.whiplash.article.repository;

import com.example.whiplash.article.document.SummarizedArticle;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SummarizedArticleRepository extends MongoRepository<SummarizedArticle, Long> {
}
