package com.example.whiplash.quiz.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.quiz.dto.request.*;
import com.example.whiplash.quiz.dto.response.MixedQuizResDto;
import com.example.whiplash.quiz.dto.response.QuizResDto;
import com.example.whiplash.quiz.dto.response.WeeklyChallengeResDto;
import com.example.whiplash.quiz.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Tag(name = "퀴즈 API", description = "용어 기반 퀴즈 생성 및 조회 API")
public class QuizController {

    private final QuizService quizService;
    private final MixedQuizService mixedQuizService;
    private final SmartMixService smartMixService;
    private final WeeklyChallengeService weeklyChallengeService;

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

    /**
     * ⭐ 신규 API: 커스텀 모의고사
     */
    @PostMapping("/mixed")
    public ApiResponse<MixedQuizResDto> createMixedQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MixedQuizReqDto request
    ) {

        Long userId = principal.getUserId();

        log.info("커스텀 모의고사 요청: userId={}, request={}", userId, request);

        MixedQuizResDto response = mixedQuizService.createMixedQuiz(userId, request);

        return ApiResponse.onSuccess(response);
    }

    /**
     * 스마트 랜덤 모의고사
     */
    @PostMapping("/smart-mix")
    public ApiResponse<MixedQuizResDto> createSmartMixQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SmartMixReqDto request
    ) {

        Long userId = principal.getUserId();

        log.info("스마트 랜덤 모의고사 요청: userId={}, totalQuestions={}",
                userId, request.getTotalQuestions());

        MixedQuizResDto response = smartMixService.createSmartMixQuiz(userId, request);

        return ApiResponse.onSuccess(response);
    }

    /**
     * ⭐ 주간 챌린지 조회
     */
    @GetMapping("/weekly-challenge")
    public ApiResponse<WeeklyChallengeResDto> getWeeklyChallenge(
            @AuthenticationPrincipal UserPrincipal principal
    ) {

        Long userId = principal.getUserId();

        log.info("주간 챌린지 조회 요청: userId={}", userId);

        WeeklyChallengeResDto response = weeklyChallengeService.getWeeklyChallenge(userId);

        return ApiResponse.onSuccess(response);
    }

    /**
     * ⭐ 주간 챌린지 제출
     */
    @PostMapping("/weekly-challenge/submit")
    public ApiResponse<WeeklyChallengeResDto.MyAttemptInfo> submitChallenge(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChallengeSubmitReqDto request
    ) {

        Long userId = principal.getUserId();

        log.info("주간 챌린지 제출 요청: userId={}, challengeId={}",
                userId, request.getChallengeId());

        WeeklyChallengeResDto.MyAttemptInfo result =
                weeklyChallengeService.submitChallenge(userId, request);

        return ApiResponse.onSuccess(result);
    }

    /**
     * 기사 기반 퀴즈 조회
     * <p>
     * GET /api/quiz/article?articleId=article123&count=5
     */
    @GetMapping("/article")
    @Operation(summary = "기사 기반 퀴즈 조회", description = "특정 기사를 기반으로 퀴즈를 생성하여 조회하는 API")
    public ApiResponse<QuizResDto> getArticleQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String articleId,
            @RequestParam(defaultValue = "3") @Min(1) @Max(10) Integer count
    ) {
        Long userId = principal.getUserId();

        log.info("기사 기반 퀴즈 조회 요청: userId={}, articleId={}, count={}",
                userId, articleId, count);

        QuizResDto response = quizService.getArticleQuiz(userId, articleId, count);

        return ApiResponse.onSuccess(response);
    }

    /**
     * ⭐ 퀴즈 캐시 초기화 (디버깅용)
     */
    @DeleteMapping("/cache")
    @Operation(summary = "퀴즈 캐시 초기화", description = "사용자의 모든 퀴즈 캐시를 삭제합니다 (역직렬화 오류 해결용)")
    public ApiResponse<String> clearQuizCache(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String term
    ) {

        Long userId = principal.getUserId();

        if (term != null && !term.isBlank()) {
            // 특정 용어의 캐시만 삭제
            mixedQuizService.clearTermQuizCache(userId, term);
            log.info("용어 퀴즈 캐시 삭제: userId={}, term={}", userId, term);
            return ApiResponse.onSuccess("용어 '" + term + "'의 캐시가 삭제되었습니다.");
        } else {
            // 모든 퀴즈 캐시 삭제
            mixedQuizService.clearUserQuizCache(userId);
            log.info("모든 퀴즈 캐시 삭제: userId={}", userId);
            return ApiResponse.onSuccess("모든 퀴즈 캐시가 삭제되었습니다.");
        }
    }
}
