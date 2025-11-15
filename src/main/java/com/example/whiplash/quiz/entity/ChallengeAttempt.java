package com.example.whiplash.quiz.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 사용자의 챌린지 도전 기록
 */
@Entity
@Table(name = "challenge_attempts",
        indexes = {
                @Index(name = "idx_user_challenge", columnList = "userId, challengeId"),
                @Index(name = "idx_challenge_score", columnList = "challengeId, score")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_challenge",
                columnNames = {"userId", "challengeId"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChallengeAttempt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long challengeId;

    @Column(nullable = false)
    private Integer score;           // 맞춘 개수

    @Column(nullable = false)
    private Integer totalQuestions;  // 전체 문제 수

    @Column(nullable = false)
    private Integer timeSpent;       // 소요 시간 (초)

    @Column(nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    protected void onCreate() {
        this.attemptedAt = LocalDateTime.now();
    }

    /**
     * 정답률 계산
     */
    public double getAccuracy() {
        return totalQuestions > 0 ? (double) score / totalQuestions * 100 : 0.0;
    }
}
