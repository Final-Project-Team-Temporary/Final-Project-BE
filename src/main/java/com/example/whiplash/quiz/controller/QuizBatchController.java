package com.example.whiplash.quiz.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.quiz.service.QuizBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/quiz-batch")
@RequiredArgsConstructor
public class QuizBatchController {

    private final QuizBatchService quizBatchService;

    /**
     * 수동으로 배치 작업 실행
     */
    @PostMapping("/run")
    public ResponseEntity<?> runBatchManually() {
        log.info("수동 배치 실행 요청");

        try {
            quizBatchService.generateQuizzesForAllActiveUsers();
            return ResponseEntity.ok(ApiResponse.onSuccess("배치 작업이 완료되었습니다."));

        } catch (Exception e) {
            log.error("배치 작업 실패", e);
            return ResponseEntity.internalServerError()
                    .body("배치 작업 실패: " + e.getMessage());
        }
    }

    /**
     * 캐시 통계 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<QuizBatchService.CacheStatistics> getStatistics() {
        QuizBatchService.CacheStatistics stats = quizBatchService.getCacheStatistics();
        return ResponseEntity.ok(stats);
    }
}
