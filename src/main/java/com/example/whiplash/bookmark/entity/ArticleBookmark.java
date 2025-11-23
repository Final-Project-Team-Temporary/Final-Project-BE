package com.example.whiplash.bookmark.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "article_bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_article",
                        columnNames = {"user_id", "article_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_article_id", columnList = "article_id"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ArticleBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "article_id", nullable = false)
    private String articleId;
}
