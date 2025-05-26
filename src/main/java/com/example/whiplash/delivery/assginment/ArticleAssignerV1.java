package com.example.whiplash.delivery.assginment;

import com.example.whiplash.article.document.Article;
import com.example.whiplash.article.document.Category;
import com.example.whiplash.article.document.SummarizedArticle;
import com.example.whiplash.article.entity.UserArticleAssignment;
import com.example.whiplash.article.repository.ArticleRepository;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.domain.entity.UserKeyword;
import com.example.whiplash.domain.entity.history.email.EmailSendStatus;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.domain.repository.UserKeywordRepository;
import com.example.whiplash.user.User;
import com.example.whiplash.user.repository.UserRepository;
import com.example.whiplash.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ArticleAssignerV1 implements ArticleAssigner{
    private final ArticleRepository articleRepository;
    private final SummarizedArticleRepository summarizedArticleRepository;
    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserService userService;
    private final InvestorProfileRepository investorProfileRepository;

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
            List<String> keywords = userKeywordRepository.findAllByUserOrderByPriority(user).stream()
                    .map(userKeyword -> userKeyword.getKeywordName())
                    .collect(Collectors.toList());

            SummaryLevel level = user.getSummaryLevel();

            List<String> interestCategories = investorProfileRepository.findByUser(user).orElseThrow().getInterestCategories().stream()
                    .map(category -> category.name())
                    .collect(Collectors.toList());

            // 유저에게 전달될 요약문
            List<SummarizedArticle> summariesToDeliver = todaySummaries.stream()
                    // 제목에 키워드 포함
                    .filter(summary -> keywords.stream().anyMatch(keyword -> summary.getTitle().contains(keyword)))
                    // 카테고리가 일치
                    .filter(summary -> interestCategories.contains(summary.getCategory()))
                    // 난이도 일치
                    .filter(summary -> summary.getSummaryLevel().equals(level))
                    .limit(3)
                    .collect(Collectors.toList());

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
