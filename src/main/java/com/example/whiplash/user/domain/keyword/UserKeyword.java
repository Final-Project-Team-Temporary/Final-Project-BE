package com.example.whiplash.user.domain.keyword;

import com.example.whiplash.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserKeyword {
    private static final int DEFAULT_PRIORITY = 0;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_keyword_id")
    private Long id;

    private Integer priority;

    private String keywordName;

    // 단방향: UserKeyword → User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 단방향: UserKeyword → Keyword
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    public static UserKeyword create(User user, Keyword keyword) {
        return UserKeyword.builder()
                .user(user)
                .keyword(keyword)
                .keywordName(keyword.getName())
                .priority(DEFAULT_PRIORITY)
                .build();
    }

    public void updatePriority(Integer priority) {
        this.priority = priority;
    }
}
