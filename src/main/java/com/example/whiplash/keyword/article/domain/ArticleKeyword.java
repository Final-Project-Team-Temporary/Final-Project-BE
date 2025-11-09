package com.example.whiplash.keyword.article.domain;

import com.example.whiplash.keyword.user.Keyword;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "article_keywords")
@Entity
public class ArticleKeyword {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String articleId; // MongoDB Article ID

    // 단방향: ArticleKeyword → Keyword
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    private String keywordName;

    public static ArticleKeyword create(String articleId, Keyword keyword) {
        return ArticleKeyword.builder()
                .articleId(articleId)
                .keyword(keyword)
                .keywordName(keyword.getName())
                .build();
    }
}


