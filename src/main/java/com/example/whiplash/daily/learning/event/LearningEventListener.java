package com.example.whiplash.daily.learning.event;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.whiplash.daily.learning.service.DailyLearningStatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LearningEventListener {

	private final DailyLearningStatService dailyLearningStatService;

	/**
	 * 학습 이벤트 핸들러
	 * 트랜잭션이 커밋된 후(이벤트 핸들러의 실행 시점을 caller의 트랜잭션 commit 이후로 지정)
	 * 비동기로 처리하여 메인 로직에 영향을 주지 않음
	 */
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleLearningEvent(LearningPerformedEvent event) {
		log.info("학습 이벤트 수신 - userId: {}, type: {}, time: {}",
			event.userId(), event.type(), event.time());

		dailyLearningStatService.recordLearning(
			event.userId(),
			event.time().toLocalDate(),
			event.type()
		);

	}
}
