package com.example.whiplash.domain.entity;

import com.example.whiplash.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_keywords")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserKeyword {
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
}
