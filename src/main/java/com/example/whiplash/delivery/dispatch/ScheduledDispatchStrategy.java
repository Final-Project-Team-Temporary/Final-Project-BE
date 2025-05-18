package com.example.whiplash.delivery.dispatch;

import com.example.whiplash.delivery.orchestrator.ArticleDeliveryOrchestrator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledDispatchStrategy implements DispatchStrategy {
    private final ArticleDeliveryOrchestrator orchestrator;

    public ScheduledDispatchStrategy(
            ArticleDeliveryOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * cron 표현식은 application.yml 에서 dispatch.scheduled.cron 으로 정의
     */
    @Override
    @Scheduled(cron = "${dispatch.scheduled.cron}")
    public void dispatchAll() {
        orchestrator.dispatchAll();
    }
}
