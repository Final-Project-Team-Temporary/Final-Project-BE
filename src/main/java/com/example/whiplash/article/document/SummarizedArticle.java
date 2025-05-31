package com.example.whiplash.article.document;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.hibernate.annotations.Index;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Document(collection = "summarized_articles")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SummarizedArticle {
    @Id
    private String id;

    @Indexed(unique = true)
    private String originalArticleId;

    private String title;

    private Category category;

    private String summarizedContent;

    private SummaryLevel summaryLevel;

    private LocalDateTime summarizedAt;

    private LocalDateTime publishedAt;

}
