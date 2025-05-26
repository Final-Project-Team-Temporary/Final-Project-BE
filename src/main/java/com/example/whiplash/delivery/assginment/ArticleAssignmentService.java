package com.example.whiplash.delivery.assginment;

import com.example.whiplash.article.entity.UserArticleAssignment;
import com.example.whiplash.article.repository.UserArticleAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ArticleAssignmentService {
    private final ArticleAssigner assigner;
    private final UserArticleAssignmentRepository assignmentRepository;

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    @Transactional
    public void assign() {
        List<UserArticleAssignment> assignments = assigner.assign();
        if (!assignments.isEmpty()) {
            assignmentRepository.saveAll(assignments);
        }

    }
}
