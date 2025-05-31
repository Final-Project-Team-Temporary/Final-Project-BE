package com.example.whiplash.article.entity;

import com.example.whiplash.domain.entity.Keyword;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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
