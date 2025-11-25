package com.example.whiplash.log.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.whiplash.log.article.entity.SummarizedArticleReadLog;

public interface SummarizedArticleReadLogRepository extends JpaRepository<SummarizedArticleReadLog, Long> {
}
