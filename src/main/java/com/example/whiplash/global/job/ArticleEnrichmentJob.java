package com.example.whiplash.global.job;

import com.example.whiplash.article.original.domain.document.Article;
import com.example.whiplash.article.original.repository.ArticleRepository;
import com.example.whiplash.article.tag.repository.ArticleMatchedKeywordRepository;
import com.example.whiplash.article.tag.service.ArticleEnrichmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEnrichmentJob implements Job {

    private final ArticleRepository articleRepository;
    private final ArticleMatchedKeywordRepository keywordRepository;
    private final ArticleEnrichmentService enrichmentService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("📊 ===== 기사 키워드/주식 태깅 배치 시작 =====");

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        try {
            // 1. 최근 1일 기사 조회
            LocalDateTime oneDaysAgo = LocalDateTime.now().minusDays(1);
            List<Article> recentArticles = articleRepository.findByPublishedAtAfter(oneDaysAgo);

            log.info("📌 최근 7일 기사 수: {}개", recentArticles.size());

            // 2. 이미 키워드가 있는 기사 ID 조회
            Set<String> enrichedArticleIds = new HashSet<>(keywordRepository.findDistinctArticleIds());

            log.info("📌 이미 처리된 기사 수: {}개", enrichedArticleIds.size());

            // 3. 미처리 기사만 필터링
            List<Article> pendingArticles = recentArticles.stream()
                    .filter(article -> !enrichedArticleIds.contains(article.getId()))
                    .toList();

            log.info("📌 처리 대상 기사 수: {}개", pendingArticles.size());

            // 4. 각 기사 처리
            for (int i = 0; i < pendingArticles.size(); i++) {
                Article article = pendingArticles.get(i);

                try {
                    log.info("🔄 처리 중 ({}/{}): articleId={}, title={}",
                            i + 1, pendingArticles.size(), article.getId(), article.getTitle());

                    // ⭐ 키워드 + 주식 동시 추출
                    enrichmentService.enrichArticleSync(article.getId());

                    successCount++;
                    log.info("✅ 처리 완료 ({}/{})", successCount, pendingArticles.size());

                    // ⭐ Rate Limiting (AI 서버 부하 방지)
                    if (i < pendingArticles.size() - 1) {
                        Thread.sleep(1000);  // 1초 대기
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("❌ 처리 실패: articleId={}, error={}",
                            article.getId(), e.getMessage());
                }
            }

            skipCount = enrichedArticleIds.size();

        } catch (Exception e) {
            log.error("❌ 배치 작업 실패", e);
            throw new JobExecutionException(e);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            log.info("📊 ===== 배치 작업 완료 =====");
            log.info("✅ 성공: {}개", successCount);
            log.info("❌ 실패: {}개", failCount);
            log.info("⏭️  스킵: {}개 (이미 처리됨)", skipCount);
            log.info("⏱️  소요 시간: {}초", duration / 1000);
        }
    }
}
