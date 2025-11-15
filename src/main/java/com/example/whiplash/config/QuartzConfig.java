package com.example.whiplash.config;

import com.example.whiplash.quiz.job.QuizPreGenerationJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "quiz.batch.enabled", havingValue = "true", matchIfMissing = true)
public class QuartzConfig {

    @Value("${quiz.batch.cron:0 0 2 * * ?}")
    private String cronExpression;

    /**
     * JobDetail 정의
     */
    @Bean
    public JobDetail quizPreGenerationJobDetail() {
        return JobBuilder.newJob(QuizPreGenerationJob.class)
                .withIdentity("quizPreGenerationJob", "quiz")
                .withDescription("매일 새벽 활성 사용자의 퀴즈를 미리 생성하는 배치 작업")
                .storeDurably()  // Job 정보를 DB에 저장
                .build();
    }

    /**
     * Trigger 정의 (실행 스케줄)
     */
    @Bean
    public Trigger quizPreGenerationTrigger() {
        log.info("퀴즈 배치 Trigger 생성: cron={}", cronExpression);

        return TriggerBuilder.newTrigger()
                .forJob(quizPreGenerationJobDetail())
                .withIdentity("quizPreGenerationTrigger", "quiz")
                .withDescription("매일 새벽 2시 실행")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronExpression)
                                .withMisfireHandlingInstructionDoNothing()  // Misfire 시 무시
                )
                .build();
    }
}
