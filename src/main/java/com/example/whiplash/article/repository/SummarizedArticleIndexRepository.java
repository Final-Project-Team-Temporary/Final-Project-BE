package com.example.whiplash.article.repository;

import com.example.whiplash.article.entity.SummarizedArticleIndex;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SummarizedArticleIndexRepository extends JpaRepository<SummarizedArticleIndex, Long> {
}
