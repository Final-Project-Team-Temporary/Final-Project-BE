package com.example.whiplash.keyword.article.streams.listener;

import java.util.List;
import java.util.Map;

import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import com.example.whiplash.keyword.article.service.ArticleKeywordService;
import com.example.whiplash.keyword.article.streams.dto.ArticleKeywordStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Streams에서 기사 키워드 추출 결과를 수신하는 Listener
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ArticleKeywordStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final ArticleKeywordService articleKeywordService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            log.info("Received article keyword stream message: id={}, stream={}", record.getId(), record.getStream());

            // 메시지 데이터 추출
            Map<String, String> messageData = record.getValue();

            // AI 서버가 보낼 메시지 형식: {"articleId": "xxx", "terms": "[\"term1\", \"term2\"]"}
            String articleId = messageData.get("articleId");
            String termsJson = messageData.get("terms");

            if (articleId == null || termsJson == null) {
                log.error("Invalid message format: missing articleId or terms. message={}", messageData);
                return;
            }

            // terms JSON 문자열을 List<String>으로 파싱
            List<String> terms = objectMapper.readValue(
                    termsJson,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            String.class
                    )
            );

            // DTO 생성 및 검증
            ArticleKeywordStreamMessage message = new ArticleKeywordStreamMessage(articleId, terms);

            log.info("Processing article keywords for articleId={}, termCount={}", articleId, terms.size());

            // 키워드 처리
            articleKeywordService.saveArticleKeywords(message.articleId(), message.terms());

            log.info("Successfully processed article keywords for articleId={}", articleId);

        } catch (Exception e) {
            log.error("Failed to process article keyword stream message: id={}, error={}",
                    record.getId(), e.getMessage(), e);
            // 여기서 DLQ(Dead Letter Queue)로 보내거나 재시도 로직 추가 가능
        }
    }
}
