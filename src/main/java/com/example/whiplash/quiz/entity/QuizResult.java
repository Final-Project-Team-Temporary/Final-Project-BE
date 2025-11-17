package com.example.whiplash.quiz.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 퀴즈 풀이 결과 저장
 */
@Entity
@Table(name = "quiz_results",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id"),
                @Index(name = "idx_user_term", columnList = "user_id, term"),
                @Index(name = "idx_solved_at", columnList = "solved_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QuizResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String term;  // 용어명 (예: "ETF")

    @Column(nullable = false)
    private Integer score;  // 맞춘 개수

    @Column(nullable = false)
    private Integer totalQuestions;  // 전체 문제 수

    @Column(nullable = false)
    private Double accuracy;  // 정답률 (계산된 값, 0.0 ~ 1.0)

    @Column(nullable = false)
    private LocalDateTime solvedAt;  // 풀이 시간

    @PrePersist
    protected void onCreate() {
        this.solvedAt = LocalDateTime.now();
        this.accuracy = totalQuestions > 0
                ? (double) score / totalQuestions
                : 0.0;
    }

}
