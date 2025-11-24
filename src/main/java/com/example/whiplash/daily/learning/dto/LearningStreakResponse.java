package com.example.whiplash.daily.learning.dto;

/**
 * 연속 학습일 조회 응답 DTO
 */
public record LearningStreakResponse(
	int consecutiveDays,
	long totalLearningDays,
	boolean learnedToday,
	int quizCount,
	int articleCount
) {
	public static LearningStreakResponse of(
		int consecutiveDays,
		long totalLearningDays,
		boolean learnedToday,
		int quizCount,
		int articleCount
	) {
		return new LearningStreakResponse(consecutiveDays,
			totalLearningDays,
			learnedToday,
			quizCount,
			articleCount);
	}
}
