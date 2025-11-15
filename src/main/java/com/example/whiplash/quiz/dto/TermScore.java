package com.example.whiplash.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 용어별 점수 (스마트 선정용)
 */
@Data
@AllArgsConstructor
public class TermScore implements Comparable<TermScore> {

    private String termName;

    private Double score;  // 가중치 합산 점수

    private LocalDateTime lastSolvedAt;  // 마지막 풀이 시간

    private Double accuracy;  // 평균 정답률

    @Override
    public int compareTo(TermScore other) {
        // 점수 높은 순 정렬
        return Double.compare(other.score, this.score);
    }
}
