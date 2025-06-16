package com.example.whiplash.delivery.orchestrator;

import com.example.whiplash.delivery.assginment.ArticleAssignmentService;
import com.example.whiplash.delivery.email.EmailSendingService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ArticleDeliveryOrchestrator {
    private final ArticleAssignmentService assignmentService;
    private final EmailSendingService emailService;

    /**
     * 전체 배포 워크플로우
     */
    public void dispatchAll() {
        // 1) 사용자별 할당 생성/갱신
        assignmentService.assign();

        // 2) 할당된 기사 요약 가져오기 (서브시스템/서비스로부터)
        //    Map<Long userId, List<SummarizedArticle>> summaries = assignmentService.fetchSummaries();

        // 3) 이메일 발송
        //    summaries.forEach((userId, list) -> emailService.sendArticleByEmail(userId, list));
    }
}
