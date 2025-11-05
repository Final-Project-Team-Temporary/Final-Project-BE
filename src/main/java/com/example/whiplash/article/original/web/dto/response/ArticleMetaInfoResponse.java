package com.example.whiplash.article.original.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMetaInfoResponse {

    private Long id;
    private String articleId;
    private String title;
    private String description;
    private LocalDateTime publishedAt;
    private String source;
    private String url;
    private String category;
    private List<String> tags;
    private String author;
    private Boolean isProcessed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}