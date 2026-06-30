package com.example.whiplash.config;

import com.example.whiplash.global.job.ArticleEnrichmentJob;
import com.example.whiplash.global.job.QuizBatchRecoveryJob;
import com.example.whiplash.global.job.QuizPreGenerationJob;
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

    @Value("${quiz.batch.recovery-cron:0 0 * * * ?}")
    private String recoveryCronExpression;

    @Bean
    public JobDetail quizPreGenerationJobDetail() {
        return JobBuilder.newJob(QuizPreGenerationJob.class)
                .withIdentity("quizPreGenerationJob", "quiz")
                .withDescription("매일 새벽 활성 사용자의 퀴즈를 미리 생성하는 배치 작업")
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail quizBatchRecoveryJobDetail() {
        return JobBuilder.newJob(QuizBatchRecoveryJob.class)
                .withIdentity("quizBatchRecoveryJob", "quiz")
                .withDescription("배치 실패 용어를 매시간 재시도하는 복구 작업")
                .storeDurably()
                .build();
    }

    @Bean
    public JobDetail articleEnrichmentJobDetail() {
        return JobBuilder.newJob(ArticleEnrichmentJob.class)
                .withIdentity("articleEnrichmentJob")
                .withDescription("최근 7일 기사 키워드/주식 태깅")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger quizPreGenerationTrigger() {
        log.info("퀴즈 배치 Trigger 생성: cron={}", cronExpression);

        return TriggerBuilder.newTrigger()
                .forJob(quizPreGenerationJobDetail())
                .withIdentity("quizPreGenerationTrigger", "quiz")
                .withDescription("매일 새벽 2시 실행")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(cronExpression)
                                // 서버 다운 후 복구 시 즉시 1회 실행 (기존: DoNothing = 스킵)
                                .withMisfireHandlingInstructionFireAndProceed()
                                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"))
                )
                .build();
    }

    @Bean
    public Trigger quizBatchRecoveryTrigger() {
        log.info("퀴즈 복구 Trigger 생성: cron={}", recoveryCronExpression);

        return TriggerBuilder.newTrigger()
                .forJob(quizBatchRecoveryJobDetail())
                .withIdentity("quizBatchRecoveryTrigger", "quiz")
                .withDescription("매시간 실패 용어 재시도")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(recoveryCronExpression)
                                .withMisfireHandlingInstructionDoNothing()
                                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"))
                )
                .build();
    }

    @Bean
    public Trigger articleEnrichmentTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(articleEnrichmentJobDetail())
                .withIdentity("articleEnrichmentTrigger")
                .withDescription("매일 새벽 3시 실행")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 3 * * ?")
                                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"))
                )
                .build();
    }
}
