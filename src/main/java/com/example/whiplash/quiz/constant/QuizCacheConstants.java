package com.example.whiplash.quiz.constant;

import java.time.Duration;

public final class QuizCacheConstants {

    private QuizCacheConstants() {}

    /** Redis L1 캐시 키 prefix — quiz:pool:{termName} */
    public static final String POOL_KEY_PREFIX = "quiz:pool:";

    /** 기사 기반 퀴즈 캐시 키 prefix — quiz:article:{articleId}:{count} */
    public static final String ARTICLE_KEY_PREFIX = "quiz:article:";

    /** 배치 실패 용어 보관 Set 키 — quiz:batch:failed */
    public static final String BATCH_FAILED_SET_KEY = "quiz:batch:failed";

    /** Redis 풀 캐시 TTL (1시간) */
    public static final Duration POOL_TTL = Duration.ofHours(1);

    /** AI 서버에서 한 번에 생성하는 퀴즈 개수 */
    public static final int POOL_SIZE = 12;

    /** 사용자에게 반환하는 기본 퀴즈 개수 */
    public static final int DEFAULT_SAMPLE_SIZE = 3;

    public static String poolKey(String termName) {
        return POOL_KEY_PREFIX + termName;
    }

    public static String articleKey(String articleId, int count) {
        return ARTICLE_KEY_PREFIX + articleId + ":" + count;
    }
}