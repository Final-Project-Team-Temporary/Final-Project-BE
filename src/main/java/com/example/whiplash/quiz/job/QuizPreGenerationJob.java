package com.example.whiplash.quiz.job;

import com.example.whiplash.quiz.service.QuizBatchService;
import com.example.whiplash.quiz.service.QuizService;
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
public class QuizPreGenerationJob implements Job {

    private final QuizBatchService quizBatchService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("========================================");
        log.info("퀴즈 미리 생성 Job 시작: {}", context.getFireTime());
        log.info("========================================");

        try {
            // 배치 서비스 호출
            quizBatchService.generateQuizzesForAllActiveUsers();

            log.info("퀴즈 미리 생성 Job 완료");

        } catch (Exception e) {
            log.error("퀴즈 미리 생성 Job 실패", e);
            throw new JobExecutionException("퀴즈 생성 배치 실패", e);
        }
    }

}
