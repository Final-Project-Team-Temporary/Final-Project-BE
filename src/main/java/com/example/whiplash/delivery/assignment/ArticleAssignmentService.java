package com.example.whiplash.delivery.assignment;

import com.example.whiplash.article.entity.UserArticleAssignment;
import com.example.whiplash.article.repository.UserArticleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ArticleAssignmentService {
    private final ArticleAssigner assigner;
    private final UserArticleAssignmentRepository assignmentRepository;

//    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")   // TODO: 로컬 테스트용
    @Transactional
    public void assign() {
        log.info("----사용자에게 기사 할당 작업: 시작");

        List<UserArticleAssignment> assignments = assigner.assign();
        if (!assignments.isEmpty()) {
            assignmentRepository.saveAll(assignments);
        }

        log.info("----사용자에게 기사 할당 작업: 종료");

    }
}
