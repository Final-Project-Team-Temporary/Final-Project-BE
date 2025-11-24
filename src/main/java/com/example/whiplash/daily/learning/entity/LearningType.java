package com.example.whiplash.daily.learning.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum LearningType {
	ARTICLE("사용자가 기사를 읽었다."),
	QUIZ("사용자가 퀴즈를 풀었다"),
	;

	private final String description;
}
