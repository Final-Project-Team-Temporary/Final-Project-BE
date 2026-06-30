package com.example.whiplash.global.job;

import com.example.whiplash.quiz.service.QuizBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class QuizBatchRecoveryJob implements Job {

    private final QuizBatchService quizBatchService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("퀴즈 배치 복구 잡 시작: {}", context.getFireTime());
        try {
            quizBatchService.retryFailedTerms();
            log.info("퀴즈 배치 복구 잡 완료");
        } catch (Exception e) {
            log.error("퀴즈 배치 복구 잡 실패", e);
            throw new JobExecutionException("퀴즈 배치 복구 실패", e);
        }
    }
}
