package com.example.whiplash.delivery.assignment;

import com.example.whiplash.IntegrationTestSupport;
import com.example.whiplash.article.domain.document.Category;
import com.example.whiplash.article.domain.document.SummarizedArticle;
import com.example.whiplash.article.domain.entity.UserArticleAssignment;
import com.example.whiplash.article.repository.ArticleRepository;
import com.example.whiplash.article.repository.SummarizedArticleRepository;
import com.example.whiplash.article.repository.UserArticleAssignmentRepository;
import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.profile.*;
import com.example.whiplash.user.repository.keyword.KeywordRepository;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.service.UserService;
import com.example.whiplash.user.web.dto.UserKeywordCreateRequest;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleAssignerV1Test extends IntegrationTestSupport {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;
    @Autowired
    private SummarizedArticleRepository summarizedArticleRepository;
    @Autowired
    private ArticleAssignerV1 articleAssignerV1;
    @Autowired
    private KeywordRepository keywordRepository;
    @Autowired
    private UserKeywordRepository userKeywordRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private InvestorProfileRepository investorProfileRepository;
    @Autowired
    private UserArticleAssignmentRepository userArticleAssignmentRepository;

    private final String userEmail="rmsghchl0@gmail.com";
    @Autowired
    private ArticleRepository articleRepository;

    @AfterEach
    void tearDown() {
        summarizedArticleRepository.deleteAll();
    }

    @DisplayName("24시간 내에 출간됐지만 조건에 맞는 요약문이 없다면 하나도 할당되지 않는다.")
    @Test
    public void should_hasSize_0_when_condition_mathced_nothing() {
        // given
        User user = saveUser();
        user.updateSummaryLevel(SummaryLevel.SHORT);
        registerUserProfile(Optional.of(userEmail), List.of(Category.GOLD));
        registerKeywords(new UserKeywordCreateRequest("비트코인"));
        registerKeywords(new UserKeywordCreateRequest("달러"));

        LocalDateTime publishedAt = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        SummarizedArticle summary1 = createSummarizedArticle(publishedAt, "하늘이 솟아오르다1", Category.GOLD, SummaryLevel.SHORT);
        SummarizedArticle summary2 = createSummarizedArticle(publishedAt, "하늘이 솟아오르다2", Category.GOLD, SummaryLevel.SHORT);
        summarizedArticleRepository.saveAll(List.of(summary1, summary2));

        // when
        articleAssignerV1.assign(publishedAt);

        // then
        List<UserArticleAssignment> assignments = userArticleAssignmentRepository.findAll();
        assertThat(assignments).hasSize(0);
    }

    @DisplayName("24시간 내에 출간된 조건에 맞는 요약문은 할당된다.")
    @Test
    public void should_hasSize_2_when_condition_matched() {
        // given
        User user = saveUser();
        user.updateSummaryLevel(SummaryLevel.SHORT);
        registerUserProfile(Optional.of(userEmail), List.of(Category.GOLD));
        registerKeywords(new UserKeywordCreateRequest("비트코인"));
        registerKeywords(new UserKeywordCreateRequest("달러"));

        LocalDateTime publishedAt = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
        SummarizedArticle summary1 = createSummarizedArticle(publishedAt, "비트코인 하늘이 솟아오르다1", Category.GOLD, SummaryLevel.SHORT);
        SummarizedArticle summary2 = createSummarizedArticle(publishedAt, "달러 하늘이 솟아오르다2", Category.GOLD, SummaryLevel.SHORT);
        summarizedArticleRepository.saveAll(List.of(summary1, summary2));

        // when
        List<UserArticleAssignment> summariesToDeliver = articleAssignerV1.assign(publishedAt);

        // then
        assertThat(summariesToDeliver).hasSize(2)
                .extracting(UserArticleAssignment::getSummarizedArticleId)
                .containsExactlyInAnyOrder(summary1.getId(), summary2.getId())
        ;
    }

    private User saveUser() {
        authService.joinUser(UserCreateDTO.builder()
                .username("id")
                .password("pw")
                .email(userEmail)
                .build()
        );
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        return user;
    }

    private void registerKeywords(UserKeywordCreateRequest keywordCreateRequest) {
        userService.createUserKeyword(keywordCreateRequest, Optional.of(userEmail));
    }

    private User registerUserProfile(Optional<String> mail, List<Category> interestCategories) {
        return userService.registerProfile(
                ProfileRegisterDTO.builder()
                        .ageRange(AgeRange.TWENTIES)
                        .investmentGoal(InvestmentGoal.EDUCATION_FUND)
                        .riskTolerance(RiskTolerance.AGGRESSIVE)
                        .investmentLevel(InvestmentLevel.BEGINNER)
                        .interestCategories(interestCategories)
                        .build(), mail
        );
    }

    private static SummarizedArticle createSummarizedArticle(LocalDateTime publishedAt, String title, Category category, SummaryLevel summaryLevel) {
        LocalDateTime summarizedAt = publishedAt.plusHours(24);
        SummarizedArticle summary = SummarizedArticle.create("1",
                title,
                category,
                "오늘 하늘이 솟아올랐다는 아주 놀라운 보고가 있다는데요. 맞나요 선생님.",
                summaryLevel,
                summarizedAt,
                publishedAt
        );
        return summary;
    }
}