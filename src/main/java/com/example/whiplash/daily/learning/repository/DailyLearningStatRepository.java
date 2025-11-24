package com.example.whiplash.daily.learning.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.whiplash.daily.learning.entity.DailyLearningStat;
import com.example.whiplash.user.domain.User;

public interface DailyLearningStatRepository extends JpaRepository<DailyLearningStat, Long> {

	/**
	 * 특정 사용자의 특정 날짜 학습 통계 조회
	 */
	Optional<DailyLearningStat> findByUserAndDate(User user, LocalDate date);

	/**
	 * 특정 사용자의 특정 날짜 학습 통계 조회 (userId로)
	 */
	@Query("SELECT d FROM DailyLearningStat d WHERE d.user.id = :userId AND d.date = :date")
	Optional<DailyLearningStat> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

	/**
	 * 특정 사용자의 날짜 범위 내 학습 통계 조회 (날짜 내림차순)
	 */
	@Query("SELECT d FROM DailyLearningStat d WHERE d.user.id = :userId AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date DESC")
	List<DailyLearningStat> findByUserIdAndDateBetween(
		@Param("userId") Long userId,
		@Param("startDate") LocalDate startDate,
		@Param("endDate") LocalDate endDate
	);

	/**
	 * 특정 사용자의 최근 N일간 학습 통계 조회 (연속 학습일 계산용)
	 */
	@Query("SELECT d FROM DailyLearningStat d WHERE d.user.id = :userId ORDER BY d.date DESC")
	List<DailyLearningStat> findRecentByUserId(@Param("userId") Long userId);

	/**
	 * 특정 사용자의 전체 학습 일수 조회
	 */
	@Query("SELECT COUNT(d) FROM DailyLearningStat d WHERE d.user.id = :userId AND (d.quizCount > 0 OR d.articleCount > 0)")
	long countLearningDaysByUserId(@Param("userId") Long userId);
}
