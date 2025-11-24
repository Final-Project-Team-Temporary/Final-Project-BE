package com.example.whiplash.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_matched_keyword")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ArticleMatchedKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private String articleId;

    private String term;

    private String termSummary;
}
