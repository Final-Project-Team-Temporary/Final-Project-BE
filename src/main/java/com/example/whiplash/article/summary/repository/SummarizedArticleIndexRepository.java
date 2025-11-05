package com.example.whiplash.article.summary.repository;

import com.example.whiplash.article.summary.domain.entity.SummarizedArticleIndex;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummarizedArticleIndexRepository extends JpaRepository<SummarizedArticleIndex, Long> {
}
