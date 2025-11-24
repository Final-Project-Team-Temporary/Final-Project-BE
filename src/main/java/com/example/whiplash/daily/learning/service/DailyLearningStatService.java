package com.example.whiplash.daily.learning.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.daily.learning.dto.DailyLearningStatDTO;
import com.example.whiplash.daily.learning.dto.LearningStreakResponse;
import com.example.whiplash.daily.learning.entity.DailyLearningStat;
import com.example.whiplash.daily.learning.entity.LearningType;
import com.example.whiplash.daily.learning.repository.DailyLearningStatRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyLearningStatService {

	private final DailyLearningStatRepository dailyLearningStatRepository;
	private final UserRepository userRepository;

	/**
	 * 학습 이벤트 처리 - 해당 날짜의 학습 통계가 없으면 생성, 있으면 업데이트
	 */
	@Transactional
	public void recordLearning(Long userId, LocalDate date, LearningType type) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		dailyLearningStatRepository.findByUserIdAndDate(userId, date)
			.ifPresentOrElse(
				stat -> stat.recordLearning(type),
				() -> {
					DailyLearningStat newStat = DailyLearningStat.create(user, date, type);
					dailyLearningStatRepository.save(newStat);
					log.info("새로운 학습 통계 생성 - userId: {}, date: {}, type: {}", userId, date, type);
				}
			);
	}

	public LearningStreakResponse getLearningStreak(Long userId) {
		int consecutiveDays = calculateConsecutiveLearningDays(userId);
		long totalLearningDays = getTotalLearningDays(userId);
		DailyLearningStatDTO stat = getDailyLearningStat(userId);

		return LearningStreakResponse.of(consecutiveDays,
			totalLearningDays,
			stat.learned(),
			stat.quizCount(),
			stat.articleCount()
		);
	}

	/**
	 * 연속 학습일 계산
	 * 오늘을 기준으로 연속으로 학습한 날 수를 계산
	 */
	public int calculateConsecutiveLearningDays(Long userId) {
		LocalDate today = LocalDate.now();

		List<DailyLearningStat> stats = dailyLearningStatRepository.findRecentByUserId(userId);

		if (stats.isEmpty()) {
			return 0;
		}

		int consecutiveDays = 0;
		LocalDate cursor = today;

		for (DailyLearningStat stat : stats) {
			if (!stat.isLearned()) {
				continue;
			}

			if (stat.getDate().equals(cursor)) {
				consecutiveDays++;
				cursor = cursor.minusDays(1);
			} else if (stat.getDate().isBefore(cursor)) {
				// 날짜가 건너뛰어졌으면 연속이 끊김
				break;
			}
		}

		return consecutiveDays;
	}

	/**
	 * 전체 학습 일수 조회
	 */
	public long getTotalLearningDays(Long userId) {
		return dailyLearningStatRepository.countLearningDaysByUserId(userId);
	}

	/**
	 * 특정 기간 내 학습 통계 조회
	 */
	public List<DailyLearningStat> getLearningStats(Long userId, LocalDate startDate, LocalDate endDate) {
		return dailyLearningStatRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
	}

	/**
	 * 오늘 학습 여부 확인
	 */
	public DailyLearningStatDTO getDailyLearningStat(Long userId) {
		LocalDate today = LocalDate.now();
		return dailyLearningStatRepository.findByUserIdAndDate(userId, today)
			.map(DailyLearningStatDTO::from)
			.orElse(DailyLearningStatDTO.notLearningStat(userId, LocalDate.now()));
	}

}
