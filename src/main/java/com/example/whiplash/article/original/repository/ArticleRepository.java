package com.example.whiplash.article.original.repository;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.domain.document.SummaryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, String> {

    List<Article> findAllByPublishedAtAfter(LocalDateTime publishedAtAfter);

    List<Article> findBySummaryStatus(SummaryStatus summaryStatus);

    Page<Article> findBySummaryStatus(SummaryStatus summaryStatus, Pageable pageable);

    List<Article> findBySummaryStatusIn(List<SummaryStatus> summaryStatuses);

    @Query("{ $or: [ { title: { $regex: ?0, $options: 'i' } }, { content: { $regex: ?0, $options: 'i' } } ], summaryStatus: 'COMPLETED' }")
    Page<Article> searchByKeyword(String keyword, Pageable pageable);

    List<Article> findByPublishedAtAfter(LocalDateTime sevenDaysAgo);
}
