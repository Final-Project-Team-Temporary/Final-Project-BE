package com.example.whiplash.quiz;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "퀴즈 API", description = "용어 기반 퀴즈 생성 및 조회 API")
public class QuizController {

    private final QuizService quizService;

    /**
     * 퀴즈 조회 API
     *
     * GET /api/quiz?userId=1001&term=ETF
     * GET /api/quiz?userId=1001  (랜덤)
     */
    @GetMapping
    @Operation(summary = "퀴즈 조회 API", description = "사용자를 위해 생성된 퀴즈를 용어로 조회하는 API")
    public ApiResponse<QuizResDto> getQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String term
    ) {

        Long userId = principal.getUserId();

        log.info("퀴즈 조회 요청: userId={}, term={}", userId, term);

        long startTime = System.currentTimeMillis();

        QuizResDto quizResponse = quizService.getQuiz(userId, term);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("퀴즈 조회 완료: userId={}, term={}, elapsed={}ms",
                userId, term, elapsedTime);

        return ApiResponse.onSuccess(quizResponse);
    }
}
