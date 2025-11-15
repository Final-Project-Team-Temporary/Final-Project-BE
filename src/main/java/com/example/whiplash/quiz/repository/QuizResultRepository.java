package com.example.whiplash.quiz.repository;

import com.example.whiplash.quiz.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    /**
     * 사용자의 모든 퀴즈 결과 조회
     */
    List<QuizResult> findByUserId(Long userId);

    /**
     * 특정 용어의 평균 정답률 조회
     */
    @Query("SELECT AVG(qr.accuracy) FROM QuizResult qr " +
            "WHERE qr.userId = :userId AND qr.term = :term")
    Double findAverageAccuracyByUserIdAndTerm(
            @Param("userId") Long userId,
            @Param("term") String term
    );

    /**
     * 특정 용어의 마지막 풀이 시간 조회
     */
    @Query("SELECT MAX(qr.solvedAt) FROM QuizResult qr " +
            "WHERE qr.userId = :userId AND qr.term = :term")
    LocalDateTime findLastSolvedAtByUserIdAndTerm(
            @Param("userId") Long userId,
            @Param("term") String term
    );

    /**
     * 정답률 낮은 용어 조회 (정답률 낮은 순)
     */
    @Query("SELECT qr.term, AVG(qr.accuracy) as avgAccuracy " +
            "FROM QuizResult qr " +
            "WHERE qr.userId = :userId " +
            "GROUP BY qr.term " +
            "HAVING AVG(qr.accuracy) < :threshold " +
            "ORDER BY avgAccuracy ASC")
    List<Object[]> findWeakTerms(
            @Param("userId") Long userId,
            @Param("threshold") Double threshold
    );

    /**
     * 특정 기간 이후 풀지 않은 용어들 (UserTerms와 조인 필요)
     */
    @Query("SELECT ut.terms.termName FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND ut.terms.termName NOT IN (" +
            "  SELECT DISTINCT qr.term FROM QuizResult qr " +
            "  WHERE qr.userId = :userId " +
            "  AND qr.solvedAt >= :since" +
            ")")
    List<String> findDormantTerms(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );
}
