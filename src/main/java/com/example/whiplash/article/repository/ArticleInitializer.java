package com.example.whiplash.article.repository;

import com.example.whiplash.article.document.Article;
import com.example.whiplash.article.document.Category;
import com.example.whiplash.article.document.SummarizedArticle;
import com.example.whiplash.article.entity.SummarizedArticleIndex;
import com.example.whiplash.domain.entity.Keyword;
import com.example.whiplash.domain.entity.UserKeyword;
import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import com.example.whiplash.domain.entity.profile.ExperienceLevel;
import com.example.whiplash.domain.entity.profile.InvestorProfile;
import com.example.whiplash.domain.entity.profile.InvestorType;
import com.example.whiplash.domain.entity.profile.RiskTolerance;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.domain.repository.KeywordRepository;
import com.example.whiplash.domain.repository.UserKeywordRepository;
import com.example.whiplash.user.Role;
import com.example.whiplash.user.User;
import com.example.whiplash.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Profile("dev")
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleInitializer {
    private final ArticleRepository articleRepository;
    private final SummarizedArticleRepository summarizedArticleRepository;
    private final UserRepository userRepository;
    private final SummarizedArticleIndexRepository summarizedArticleIndexRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final KeywordRepository keywordRepository;
    private final InvestorProfileRepository investorProfileRepository;

    @PostConstruct
    @Transactional
    void init() {
        log.info("----Initializer 초기 세팅 시작");

        // === Article 저장 ===
        String press = "jtbc";
        String title1 = " title1 gold ";
        Category category = Category.GOLD;
        LocalDateTime publishedAt = LocalDateTime.now().minusHours(4);

        Article article = articleRepository.findAll().stream().findFirst().orElseGet(() -> {
            Article newArticle = Article.builder()
                    .title(title1)
                    .category(category)
                    .press("jtbc")
                    .publishedAt(publishedAt)
                    .build();
            return articleRepository.save(newArticle);
        });

        // === ArticleIndex 저장 ===
        if (summarizedArticleIndexRepository.findAll().isEmpty()) {
            SummarizedArticleIndex summarizedArticleIndex = SummarizedArticleIndex.builder()
                    .title(title1)
                    .press(press)
                    .publishedAt(publishedAt)
                    .createdAt(LocalDateTime.now())
                    .originalMongoId(article.getId())
                    .build();
            summarizedArticleIndexRepository.save(summarizedArticleIndex);
        }

        // === SummarizedArticle 저장 ===
        if (summarizedArticleRepository.findAll().isEmpty()) {
            for (SummaryLevel level : SummaryLevel.values()) {
                for (int i = 1; i <= 2; i++) {
                    SummarizedArticle summarizedArticle = SummarizedArticle.builder()
                            .originalArticleId(article.getId()) // 기존 article 참조
                            .title("[" + level.name() + "] 요약 제목 " + title1 + i)
                            .category(article.getCategory())
                            .summarizedContent("이것은 " + level.name() + " 수준의 요약 내용입니다. 번호: " + i)
                            .summaryLevel(level)
                            .summarizedAt(LocalDateTime.now().minusMinutes(i * 10L))
                            .publishedAt(article.getPublishedAt())
                            .build();

                    summarizedArticleRepository.save(summarizedArticle);
                }
            }
        }


        // === Keyword 저장 ===
        if (keywordRepository.findAll().isEmpty()) {
            List<Keyword> keywords = List.of("금리", "테슬라", "반도체", "환율", "미국 시장", "gold").stream()
                    .map(name -> Keyword.builder().name(name).build())
                    .toList();
            keywordRepository.saveAll(keywords);
        }
        List<Keyword> savedKeywords = keywordRepository.findAll();

        // === User 저장 ===
        if (userRepository.findAll().isEmpty()) {
            List<User> users = List.of(
                    User.builder().name("Alice").email("alice@test.com").age(25).role(Role.USER).summaryLevel(SummaryLevel.SHORT).build(),
                    User.builder().name("Bob").email("bob@test.com").age(30).role(Role.USER).summaryLevel(SummaryLevel.SHORT).build(),
                    User.builder().name("Charlie").email("charlie@test.com").age(28).role(Role.USER).summaryLevel(SummaryLevel.MEDIUM).build(),
                    User.builder().name("David").email("rmsghchl0@naver.com").age(35).role(Role.USER).summaryLevel(SummaryLevel.MEDIUM).build(),
                    User.builder().name("Eve").email("eve@test.com").age(27).role(Role.USER).summaryLevel(SummaryLevel.MEDIUM).build()
            );
            userRepository.saveAll(users);
        }
        List<User> savedUsers = userRepository.findAll();

        // === UserKeyword 저장 ===
        if (userKeywordRepository.findAll().isEmpty()) {
            int keywordSize = savedKeywords.size();

            for (int i = 0; i < savedUsers.size(); i++) {
                User user = savedUsers.get(i);
                for (int j = 0; j < 3; j++) { // 사용자당 3개 키워드 할당
                    Keyword keyword = savedKeywords.get((i + j) % keywordSize);
                    userKeywordRepository.save(
                            UserKeyword.builder()
                                    .user(user)
                                    .keyword(keyword)
                                    .keywordName(keyword.getName())
                                    .priority(j + 1)
                                    .build()
                    );
                }
            }
        }

        // === InvestorProfile 저장 ===
        if (investorProfileRepository.findAll().isEmpty()) {
            for (int i = 0; i < savedUsers.size(); i++) {
                User user = savedUsers.get(i);

                InvestorProfile profile = InvestorProfile.builder()
                        .user(user)
                        .investorType(i % 2 == 0 ? InvestorType.AGGRESSIVE : InvestorType.CONSERVATIVE)
                        .riskTolerance(i % 3 == 0 ? RiskTolerance.HIGH : (i % 3 == 1 ? RiskTolerance.MEDIUM : RiskTolerance.LOW))
                        .investmentPeriod(3 + i)  // 3~7년
                        .expectedReturn(5 + i * 2) // 5%~13%
                        .experienceLevel(i < 2 ? ExperienceLevel.BEGINNER : (i == 2 ? ExperienceLevel.INTERMEDIATE : ExperienceLevel.EXPERT))
                        .interestCategories(List.of(
                                Category.GOLD,
                                i % 2 == 0 ? Category.BATTERY : Category.MEDICINE
                        ))
                        .build();

                investorProfileRepository.save(profile);
            }
        }

        log.info("----Initializer 초기 세팅 완료");
    }





}
