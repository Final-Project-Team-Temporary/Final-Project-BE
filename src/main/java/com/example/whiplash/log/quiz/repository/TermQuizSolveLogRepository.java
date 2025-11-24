package com.example.whiplash.log.quiz.repository;

import com.example.whiplash.log.quiz.entity.TermQuizSolveLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TermQuizSolveLogRepository extends JpaRepository<TermQuizSolveLog, Long> {

    List<TermQuizSolveLog> findByUserId(Long userId);

    /**
     * 특정 용어의 평균 정답률 조회
     */
    @Query("SELECT AVG(CASE WHEN r.isCorrect = true THEN 1.0 ELSE 0.0 END) " +
            "FROM TermQuizSolveLog t JOIN t.results r " +
            "WHERE t.userId = :userId AND r.term = :term")
    Double findAverageAccuracyByUserIdAndTerm(
            @Param("userId") Long userId,
            @Param("term") String term
    );

    /**
     * 특정 용어의 마지막 풀이 시간 조회
     */
    @Query("SELECT MAX(t.solvedAt) FROM TermQuizSolveLog t JOIN t.results r " +
            "WHERE t.userId = :userId AND r.term = :term")
    LocalDateTime findLastSolvedAtByUserIdAndTerm(
            @Param("userId") Long userId,
            @Param("term") String term
    );

    /**
     * 정답률 낮은 용어 조회 (정답률 낮은 순)
     */
    @Query("SELECT r.term, AVG(CASE WHEN r.isCorrect = true THEN 1.0 ELSE 0.0 END) as avgAccuracy " +
            "FROM TermQuizSolveLog t JOIN t.results r " +
            "WHERE t.userId = :userId " +
            "GROUP BY r.term " +
            "HAVING AVG(CASE WHEN r.isCorrect = true THEN 1.0 ELSE 0.0 END) < :threshold " +
            "ORDER BY avgAccuracy ASC")
    List<Object[]> findWeakTerms(
            @Param("userId") Long userId,
            @Param("threshold") Double threshold
    );

    /**
     * 특정 기간 이후 풀지 않은 용어들
     */
    @Query("SELECT ut.terms.termName FROM UserTerms ut " +
            "WHERE ut.user.id = :userId " +
            "AND ut.terms.termName NOT IN (" +
            "  SELECT DISTINCT r.term FROM TermQuizSolveLog t JOIN t.results r " +
            "  WHERE t.userId = :userId " +
            "  AND t.solvedAt >= :since" +
            ")")
    List<String> findDormantTerms(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );
}
