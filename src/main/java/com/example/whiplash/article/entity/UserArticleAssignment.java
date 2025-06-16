package com.example.whiplash.article.entity;

import com.example.whiplash.domain.entity.history.email.EmailSendStatus;
import com.example.whiplash.user.User;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_article_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "summarized_article_id"})   //TODO
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserArticleAssignment {
    @Id
    @GeneratedValue
    private Long id;

    // 단방향: Assignment → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 할당된 요약 아티클 ID (Mongo의 SummarizedArticle.id)
    private String summarizedArticleId;

    @Enumerated(EnumType.STRING)
    private EmailSendStatus sendStatus;

    private LocalDateTime assignedAt;

    public void updateStatus(EmailSendStatus status) {
        this.sendStatus = status;
    }
}
