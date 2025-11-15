package com.example.whiplash.quiz.dto.response;

import com.example.whiplash.quiz.entity.ChallengeAttempt;
import com.example.whiplash.quiz.entity.WeeklyChallenge;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class WeeklyChallengeResDto {

    private ChallengeInfo challenge;          // 챌린지 정보
    private MyAttemptInfo myAttempt;          // 내 도전 기록
    private List<MixedQuizResDto.QuizWithTerm> quizzes;       // 퀴즈 데이터 (도전 안 했으면)
    private List<RankingEntry> ranking;       // 상위 10명 랭킹
    private StatsInfo stats;                  // 통계

    @Data
    @AllArgsConstructor
    public static class ChallengeInfo {
        private Long id;
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private Integer totalQuestions;
        private Integer timeLimit;
        private String terms;
        private Boolean isActive;

        public static ChallengeInfo from(WeeklyChallenge challenge, String termsStr) {
            return new ChallengeInfo(
                    challenge.getId(),
                    challenge.getWeekStartDate(),
                    challenge.getWeekEndDate(),
                    challenge.getTotalQuestions(),
                    challenge.getTimeLimit(),
                    termsStr,
                    challenge.isActive()
            );
        }
    }

    @Data
    @AllArgsConstructor
    public static class MyAttemptInfo {
        private Integer score;
        private Integer totalQuestions;
        private Integer timeSpent;
        private Double accuracy;
        private Integer rank;
        private LocalDateTime attemptedAt;

        public static MyAttemptInfo from(ChallengeAttempt attempt, int rank) {
            return new MyAttemptInfo(
                    attempt.getScore(),
                    attempt.getTotalQuestions(),
                    attempt.getTimeSpent(),
                    attempt.getAccuracy(),
                    rank,
                    attempt.getAttemptedAt()
            );
        }
    }

    @Data
    @AllArgsConstructor
    public static class RankingEntry {
        private Integer rank;
        private Long userId;
        private String username;  // 향후 User 엔티티에 추가
        private Integer score;
        private Integer totalQuestions;
        private Integer timeSpent;
        private Double accuracy;
    }

    @Data
    @AllArgsConstructor
    public static class StatsInfo {
        private Long totalParticipants;  // 전체 참여자 수
        private Double averageScore;     // 평균 점수
    }

}
