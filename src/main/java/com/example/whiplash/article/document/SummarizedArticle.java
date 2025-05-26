package com.example.whiplash.article.document;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;

@Document(collection = "summarized_articles")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SummarizedArticle {
    @Id
    private Long id;
    private String articleId;
    private String summarizedContent;
    private SummaryLevel summaryLevel;
    private LocalDateTime summarizedAt;
}
