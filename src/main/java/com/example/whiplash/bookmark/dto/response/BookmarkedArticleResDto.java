package com.example.whiplash.bookmark.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class BookmarkedArticleResDto {
    private String id;
    private String title;
    private LocalDateTime publishedAt;
}
