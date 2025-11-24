package com.example.whiplash.daily.learning.dto;

import java.time.LocalDate;

import com.example.whiplash.daily.learning.entity.DailyLearningStat;

public record DailyLearningStatDTO(
	Long id,
	Long userId,
	LocalDate date,
	int quizCount,
	int articleCount,
	boolean learned
) {
	public static DailyLearningStatDTO from(DailyLearningStat stat) {
		return new DailyLearningStatDTO(stat.getId(),
			stat.getUser().getId(),
			stat.getDate(),
			stat.getQuizCount(),
			stat.getArticleCount(),
			stat.isLearned()
		);
	}

	public static DailyLearningStatDTO notLearningStat(Long userId, LocalDate date) {
		return new DailyLearningStatDTO(null,
			userId,
			date,
			0,
			0,
			false
		);
	}
}
