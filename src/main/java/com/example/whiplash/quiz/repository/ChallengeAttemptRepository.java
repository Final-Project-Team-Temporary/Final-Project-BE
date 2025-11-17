package com.example.whiplash.quiz.repository;

import com.example.whiplash.quiz.entity.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Long> {

    /**
     * 특정 사용자의 특정 챌린지 도전 기록 조회
     */
    Optional<ChallengeAttempt> findByUserIdAndChallengeId(Long userId, Long challengeId);

    /**
     * 특정 챌린지의 상위 N명 랭킹 조회
     */
    @Query("SELECT ca FROM ChallengeAttempt ca " +
            "WHERE ca.challengeId = :challengeId " +
            "ORDER BY ca.score DESC, ca.timeSpent ASC")
    List<ChallengeAttempt> findTop10ByChallengeIdOrderByScoreDesc(
            @Param("challengeId") Long challengeId
    );

    /**
     * 특정 챌린지의 전체 참여자 수
     */
    long countByChallengeId(Long challengeId);

    /**
     * 특정 사용자의 순위 계산
     */
    @Query("SELECT COUNT(ca) + 1 FROM ChallengeAttempt ca " +
            "WHERE ca.challengeId = :challengeId " +
            "AND (ca.score > :score OR (ca.score = :score AND ca.timeSpent < :timeSpent))")
    int calculateRank(
            @Param("challengeId") Long challengeId,
            @Param("score") Integer score,
            @Param("timeSpent") Integer timeSpent
    );
}
