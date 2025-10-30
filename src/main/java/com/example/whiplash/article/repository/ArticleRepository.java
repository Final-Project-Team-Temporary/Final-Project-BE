package com.example.whiplash.article.repository;

import com.example.whiplash.article.domain.document.Article;
import com.example.whiplash.article.domain.document.SummaryStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, String> {

    List<Article> findBySummaryStatus(SummaryStatus summaryStatus);
}
