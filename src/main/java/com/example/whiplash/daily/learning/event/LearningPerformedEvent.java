package com.example.whiplash.daily.learning.event;

import java.time.LocalDateTime;

import com.example.whiplash.daily.learning.entity.LearningType;

public record LearningPerformedEvent(
	Long userId,
	LocalDateTime time,
	LearningType type
) {
}
