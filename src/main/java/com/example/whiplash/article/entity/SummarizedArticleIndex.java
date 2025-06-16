package com.example.whiplash.article.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "article_indices")
@Entity
public class SummarizedArticleIndex {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String press;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private String originalMongoId;
}
