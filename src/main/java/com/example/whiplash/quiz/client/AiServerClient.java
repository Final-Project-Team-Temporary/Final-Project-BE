package com.example.whiplash.quiz.client;

import com.example.whiplash.quiz.client.dto.KeywordExtractionResponse;
import com.example.whiplash.quiz.client.dto.KeywordTermDto;
import com.example.whiplash.quiz.client.dto.StockDto;
import com.example.whiplash.quiz.client.dto.StockExtractionResponse;
import com.example.whiplash.quiz.dto.request.ArticleQuizCreateReqDto;
import com.example.whiplash.quiz.dto.request.QuizCreateReqDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.term.dto.request.TermExplainReqDto;
import com.example.whiplash.term.dto.response.TermExplainResDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiServerClient {

    private final WebClient webClient;

    public AiServerClient(
            @Value("${ai.server.base-url}") String baseUrl,
            @Value("${ai.server.timeout:10000}") int timeout
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * AI 서버에 퀴즈 생성 요청
     */
    public QuizResDto generateQuiz(String keyword, int count) {
        log.info("AI 서버 퀴즈 생성 요청: keyword={}, count={}", keyword, count);

        QuizCreateReqDto request = new QuizCreateReqDto(keyword, count);

        try {
            QuizResDto response = webClient.post()
                    .uri("/quiz/by-keyword")  // AI 서버 엔드포인트
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("AI 서버 에러: status={}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("AI 서버 응답 실패"));
                    })
                    .bodyToMono(QuizResDto.class)
                    .timeout(Duration.ofSeconds(100))
                    .block();  // 동기 방식으로 대기

            if (response != null) {
                response.setTerm(keyword);
                log.info("AI 서버 퀴즈 생성 완료: keyword={}, quizCount={}",
                        keyword, response.getQuizzes().size());
            }

            return response;

        } catch (Exception e) {
            log.error("AI 서버 통신 실패: keyword={}", keyword, e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * AI 서버에 용어 설명 요청
     */
    public TermExplainResDto getTermExplain(String term) {
        log.info("AI 서버 용어 설명 요청 : {}", term);

        TermExplainReqDto request = new TermExplainReqDto(term);

        try {
            TermExplainResDto response = webClient.post()
                    .uri("/keyword/define")  // AI 서버 엔드포인트
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("AI 서버 에러: status={}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("AI 서버 응답 실패"));
                    })
                    .bodyToMono(TermExplainResDto.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();  // 동기 방식으로 대기

            if (response != null) {
                log.info("AI 용어 설명 완료 : {}", response.getDefinition());
            }

            return response;

        } catch (Exception e) {
            log.error("AI 서버 통신 실패: keyword={}", term, e);
            throw new RuntimeException("용어 설명 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기사 기반 퀴즈 생성
     */
    public QuizResDto getQuizzesByArticle(String articleId, int count) {
        log.info("AI 서버 퀴즈 생성 요청: articleId={}, count={}", articleId, count);

        ArticleQuizCreateReqDto request = new ArticleQuizCreateReqDto(articleId, count);

        try {
            QuizResDto response = webClient.post()
                    .uri("/quiz/by-article")  // AI 서버 엔드포인트
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse -> {
                        log.error("AI 서버 에러: status={}", clientResponse.statusCode());
                        return Mono.error(new RuntimeException("AI 서버 응답 실패"));
                    })
                    .bodyToMono(QuizResDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();  // 동기 방식으로 대기

            if (response != null) {
                log.info("AI 서버 퀴즈 생성 완료:  quizCount={}",
                        response.getQuizzes().size());
            }

            return response;

        } catch (Exception e) {
            log.error("AI 서버 통신 실패: articleId={}", articleId, e);
            throw new RuntimeException("퀴즈 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 기사 키워드 추출
     * @param title
     * @param content
     * @return
     */
    public List<KeywordTermDto> extractKeywordsFromArticle(String title, String content) {
        log.info("AI 서버 키워드 추출 요청: title={}", title);

        Map<String, String> requestBody = Map.of(
                "title", title,
                "content", content
        );

        try {
            KeywordExtractionResponse response = webClient.post()
                    .uri("/keyword/terms")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(KeywordExtractionResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getResults() != null) {
                log.info("키워드 추출 완료: count={}", response.getResults().size());
                return response.getResults();
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("키워드 추출 실패: title={}", title, e);
            return Collections.emptyList();
        }

    }

    /**
     * 기사 기반 주식 종목 추출
     * @param title
     * @param content
     * @return
     */
    public List<StockDto> extractStocksFromArticle(String title, String content) {
        log.info("AI 서버 주식 종목 추출 요청: title={}", title);

        Map<String, String> requestBody = Map.of(
                "title", title,
                "content", content
        );

        try {
            StockExtractionResponse response = webClient.post()
                    .uri("/keyword/stock_id")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(StockExtractionResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            if (response != null && response.getMatchedStocks() != null) {
                log.info("주식 추출 완료: count={}", response.getMatchedStocks().size());
                return response.getMatchedStocks();
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("주식 추출 실패: title={}", title, e);
            return Collections.emptyList();
        }
    }
}
