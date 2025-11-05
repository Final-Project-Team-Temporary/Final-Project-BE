package com.example.whiplash.article.summary.domain.entity;

import com.example.whiplash.user.domain.keyword.Keyword;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "article_keywords")
@Entity
public class ArticleKeyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 단방향: ArticleKeyword → ArticleIndex
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_index_id", nullable = false)
    private SummarizedArticleIndex summarizedArticleIndex;

    // 단방향: ArticleKeyword → Keyword
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;
}


