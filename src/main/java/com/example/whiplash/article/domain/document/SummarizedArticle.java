package com.example.whiplash.article.domain.document;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Document(collection = "summarized_articles")
public class SummarizedArticle {
    @Id
    private String id;

//    @Indexed(unique = true)
    private String originalArticleId;

    private String title;

    private Category category;

    private String summarizedContent;

    private SummaryLevel summaryLevel;

    private LocalDateTime summarizedAt;

    private LocalDateTime publishedAt;

    public static SummarizedArticle create(String originalArticleId,
                                           String title,
                                           Category category,
                                           String summarizedContent,
                                           SummaryLevel summaryLevel,
                                           LocalDateTime summarizedAt,
                                           LocalDateTime publishedAt) {
        return SummarizedArticle.builder()
                .originalArticleId(originalArticleId)
                .title(title)
                .category(category)
                .summarizedContent(summarizedContent)
                .summaryLevel(summaryLevel)
                .summarizedAt(summarizedAt)
                .publishedAt(publishedAt)
                .build();
    }
}
