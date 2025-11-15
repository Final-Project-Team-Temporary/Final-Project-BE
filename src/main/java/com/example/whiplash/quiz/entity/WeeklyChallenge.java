package com.example.whiplash.quiz.entity;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 주간 챌린지 (매주 월요일 생성)
 */
@Entity
@Table(name = "weekly_challenges",
        indexes = @Index(name = "idx_week_start", columnList = "weekStartDate"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyChallenge extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate weekStartDate;  // 월요일 날짜 (예: 2025-11-11)

    @Column(nullable = false)
    private LocalDate weekEndDate;    // 일요일 날짜 (예: 2025-11-17)

    @Column(nullable = false)
    private Integer totalQuestions;   // 전체 문제 수

    @Column(nullable = false)
    private Integer timeLimit;        // 제한 시간 (분)

    @Column(columnDefinition = "TEXT")
    private String termsJson;         // 출제된 용어 목록 (JSON)

    @Column(columnDefinition = "LONGTEXT")
    private String quizzesJson;       // 퀴즈 데이터 (JSON)

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 챌린지 기간 확인
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(weekStartDate) && !today.isAfter(weekEndDate);
    }
}
