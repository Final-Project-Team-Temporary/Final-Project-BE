package com.example.whiplash.quiz.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 애플리케이션 기동 시 quiz 관련 MongoDB 인덱스를 보장하는 초기화 클래스.
 *
 * <ul>
 *   <li>term_quiz_pools.created_at → TTL 90일 (만료된 퀴즈 풀 자동 삭제)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuizIndexInitializer implements InitializingBean {

    private final MongoTemplate mongoTemplate;

    @Override
    public void afterPropertiesSet() {
        mongoTemplate.indexOps("term_quiz_pools")
                .ensureIndex(new Index("created_at", Sort.Direction.ASC)
                        .expire(90, TimeUnit.DAYS));
        log.info("term_quiz_pools TTL 인덱스 등록 완료 (90일)");
    }
}