package com.example.whiplash.delivery.orchestrator;

import com.example.whiplash.article.entity.UserArticleAssignment;
import com.example.whiplash.delivery.assignment.UserArticleAssignmentService;
import com.example.whiplash.delivery.email.EmailSendingService;
import com.example.whiplash.domain.entity.history.email.EmailSendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ArticleDeliveryOrchestrator {
    private final UserArticleAssignmentService assignmentService;
    private final EmailSendingService emailService;

    /**
     * 전체 배포 워크플로우
     */
    public void dispatchAll() {
        // 1) 사용자별 할당 생성/갱신
        assignmentService.assign();

        // 2) 할당된 기사 요약 가져오기 (서브시스템/서비스로부터)
        List<UserArticleAssignment> assignments = assignmentService.getArticleAssignmentsByStatus(EmailSendStatus.NEED_TO_SEND);
        Map<Long, List<Long>> assignmentIdsByUser = assignments.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getUser().getId(),
                        Collectors.mapping(assignment -> assignment.getId(), Collectors.toList())
                ));

        // 3) 이메일 발송
        Map<Long, List<String>> summaryIdsByUser = assignments.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getUser().getId(), //userId로 그룹핑
                        Collectors.mapping(assignment -> assignment.getSummarizedArticleId(), Collectors.toList())  // 어떻게 그룹핑 할지
                ));
        summaryIdsByUser.forEach((userId, summaryIds) -> {
                    emailService.sendSummarizedArticlesToUser(userId, summaryIds);                                      // 이메일 전송
                    assignmentService.updateAssignmentStatus(assignmentIdsByUser.get(userId), EmailSendStatus.SENT);    //UserArticleAssignment 상태 변경
                }
        );

    }
    /** TODO
     * (옵션) 대량 트래픽 시 고려해볼 추가 최적화
     * 페이징 처리: getArticleAssignmentsByStatus 결과가 수천 건 이상이라면 페이징 또는 배치 처리.
     *
     * 비동기/배치 큐: 대량 메일 발송 시, 비동기로 emailService 호출하거나 메시지 큐에 태스크를 보내는 구조로 변경.
     */
}
