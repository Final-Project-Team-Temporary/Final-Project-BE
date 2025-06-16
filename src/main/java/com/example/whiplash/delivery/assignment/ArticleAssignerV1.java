package com.example.whiplash.delivery.assignment;

import com.example.whiplash.article.document.Category;
import com.example.whiplash.article.document.SummarizedArticle;
import com.example.whiplash.article.entity.UserArticleAssignment;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.article.repository.UserArticleAssignmentRepository;
import com.example.whiplash.domain.entity.history.email.EmailSendStatus;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.domain.repository.UserKeywordRepository;
import com.example.whiplash.user.User;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleAssignerV1 implements ArticleAssigner{
    private final SummarizedArticleRepository summarizedArticleRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final UserArticleAssignmentRepository userArticleAssignmentRepository;

    @Override
    public List<UserArticleAssignment> assign() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime publishedAfter = now.minusDays(1);

        // 할당 대상: 24시간 이내에 발행된 기사
        List<SummarizedArticle> todaySummaries = summarizedArticleRepository.findAllByPublishedAtAfter(publishedAfter);

        // 모든 유저 조회
        List<User> findUsers = userRepository.findAll();

        List<UserArticleAssignment> assignments = new ArrayList<>();

        //할당 기준필터를 동작시킨다.
        for (User user : findUsers) {
            // 사용자 정보 꺼내기
            List<String> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user).stream()
                    .map(userKeyword -> userKeyword.getKeywordName())
                    .collect(Collectors.toList());

            SummaryLevel userSummaryLevel = user.getSummaryLevel();

            List<Category> userInterestCategories = investorProfileRepository.findByUser(user).orElseThrow().getInterestCategories();

            // 유저에게 전달될 요약문
             /**
             TODO
              1. 현재는 SummarizedArticle 객체를 모두 가져와서 처리중. 하지만 이러면 너무 많은 메모리 사용할듯
              => ArticleIndex 사용하는 방향으로
              2. 기사 할당 중복처리 개선
              기존: UserArticleAssignment를 조회한 후에 없는 대상만 할당하고 있음.
              변경: insert ignore into를 활용
             *  */
            log.info("사용자: {}", user.getName());
            log.info("유저 키워드: {}", userKeywords);
            log.info("유저 관심 카테고리: {}", userInterestCategories);

            List<String> assignedSummaryIds = userArticleAssignmentRepository.findAllByUser(user).stream()
                    .map(userArticleAssignment -> userArticleAssignment.getSummarizedArticleId())
                    .collect(Collectors.toList());

            List<SummarizedArticle> summariesToDeliver = todaySummaries.stream()
                    // 제목에 키워드 포함
                    .filter(summary -> userKeywords.stream().anyMatch(keyword -> summary.getTitle().contains(keyword)))
                    // 카테고리가 일치
                    .filter(summary -> userInterestCategories.contains(summary.getCategory()))
                    // 난이도 일치
                    .filter(summary -> summary.getSummaryLevel().equals(userSummaryLevel))
                    .filter(summary-> !assignedSummaryIds.contains(summary.getId()))    //기사 중복 할당 방지
                    .limit(3)
                    .collect(Collectors.toList());


            log.info("summariesToDeliver: {}", summariesToDeliver);

            for (SummarizedArticle summary : summariesToDeliver) {
                assignments.add(
                        UserArticleAssignment.builder()
                        .user(user)
                        .assignedAt(now)
                        .summarizedArticleId(summary.getId())
                        .sendStatus(EmailSendStatus.PENDING)
                        .build());
            }
        }

        return assignments;
    }
}
