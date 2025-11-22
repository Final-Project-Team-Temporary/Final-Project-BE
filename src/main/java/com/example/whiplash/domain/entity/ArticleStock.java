package com.example.whiplash.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article_stocks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ArticleStock extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private String articleId;

    private String stockName;

    private String stockCode;

    private String market;  // KOSPI, KOSDAQ

    private String sector;
}
