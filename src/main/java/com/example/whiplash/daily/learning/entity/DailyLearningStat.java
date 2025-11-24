package com.example.whiplash.daily.learning.entity;

import java.time.LocalDate;

import com.example.whiplash.domain.entity.BaseEntity;
import com.example.whiplash.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Learning 정의 = { 기사를 적어도 한번 읽는다 || 퀴즈를 적어도 한번 푼다}
 * 사용자별 일별 학습 통계를 관리하는 엔티티
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(
	name = "daily_learning_stat",
	uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}),
	indexes = @Index(name = "idx_daily_learning_stat_user_date", columnList = "user_id, date DESC")
)
public class DailyLearningStat extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JoinColumn(name = "user_id", nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private int quizCount;

	@Column(nullable = false)
	private int articleCount;

	/**
	 * 퀴즈 학습 기록 추가
	 */
	private void incrementQuizCount() {
		this.quizCount++;
	}

	/**
	 * 기사 학습 기록 추가
	 */
	private void incrementArticleCount() {
		this.articleCount++;
	}

	/**
	 * 학습 타입에 따라 카운트 증가
	 */
	public void recordLearning(LearningType type) {
		switch (type) {
			case QUIZ -> incrementQuizCount();
			case ARTICLE -> incrementArticleCount();
		}
	}

	/**
	 * 오늘 학습을 했는지 여부 (퀴즈 또는 기사 중 하나라도 학습)
	 */
	public boolean isLearned() {
		return quizCount > 0 || articleCount > 0;
	}

	/**
	 * 새로운 DailyLearningStat 생성 (정적 팩토리 메서드)
	 */
	public static DailyLearningStat create(User user, LocalDate date, LearningType initialType) {
		DailyLearningStat stat = DailyLearningStat.builder()
			.user(user)
			.date(date)
			.quizCount(0)
			.articleCount(0)
			.build();
		stat.recordLearning(initialType);
		return stat;
	}
}
