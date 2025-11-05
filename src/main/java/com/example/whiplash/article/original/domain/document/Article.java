package com.example.whiplash.article.original.domain.document;

import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {
    @Id @GeneratedValue
    private String id;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private String url;
    private String press;
    @Indexed
    private SummaryStatus summaryStatus;
    
    // 개별 setter: 요약 상태 업데이트용
    public void setSummaryStatus(SummaryStatus summaryStatus) {
        this.summaryStatus = summaryStatus;
    }
//    private Category category;

}