package com.example.whiplash.log.quiz.controller;

import java.time.LocalDateTime;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.log.quiz.dto.request.TermQuizSolveRequest;
import com.example.whiplash.log.quiz.dto.response.TermQuizSolveResponse;
import com.example.whiplash.log.quiz.service.QuizSolveLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "퀴즈 풀이 기록 API", description = "퀴즈 풀이 결과 저장 API")
public class QuizSolveLogController {

    private final QuizSolveLogService quizSolveLogService;

    @PostMapping("/results")
    @Operation(summary = "퀴즈 결과 저장", description = "퀴즈 풀이 결과를 저장합니다")
    public ApiResponse<TermQuizSolveResponse> solveTermQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody TermQuizSolveRequest request
    ) {
        Long userId = principal.getUserId();

        log.info("퀴즈 결과 저장 요청: userId={}, totalQuestions={}", userId, request.results().size());

        TermQuizSolveResponse response = quizSolveLogService.solveTermQuiz(userId, request, LocalDateTime.now());

        return ApiResponse.onSuccess(response);
    }
}
