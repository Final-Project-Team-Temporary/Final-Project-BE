package com.example.whiplash.log.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.whiplash.log.article.entity.ArticleReadLog;

public interface ArticleReadLogRepository extends JpaRepository<ArticleReadLog, Long> {
}
