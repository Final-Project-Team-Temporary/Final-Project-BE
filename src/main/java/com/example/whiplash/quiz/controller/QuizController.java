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

    @GetMapping
    @Operation(summary = "퀴즈 조회", description = "용어별 퀴즈 풀에서 랜덤 샘플링하여 반환")
    public ApiResponse<QuizResDto> getQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String term
    ) {
        Long userId = principal.getUserId();
        log.info("퀴즈 조회 요청: userId={}, term={}", userId, term);
        return ApiResponse.onSuccess(quizService.getQuiz(userId, term));
    }

    @PostMapping("/mixed")
    @Operation(summary = "커스텀 모의고사 생성", description = "지정한 용어들의 퀴즈 풀에서 샘플링하여 모의고사 구성")
    public ApiResponse<MixedQuizResDto> createMixedQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MixedQuizReqDto request
    ) {
        Long userId = principal.getUserId();
        log.info("커스텀 모의고사 요청: userId={}, terms={}", userId, request.getTerms());
        return ApiResponse.onSuccess(mixedQuizService.createMixedQuiz(userId, request));
    }

    @PostMapping("/smart-mix")
    @Operation(summary = "스마트 랜덤 모의고사", description = "정답률·학습 이력 기반으로 용어를 선정하여 모의고사 구성")
    public ApiResponse<MixedQuizResDto> createSmartMixQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SmartMixReqDto request
    ) {
        Long userId = principal.getUserId();
        log.info("스마트 모의고사 요청: userId={}, totalQuestions={}", userId, request.getTotalQuestions());
        return ApiResponse.onSuccess(smartMixService.createSmartMixQuiz(userId, request));
    }

    @GetMapping("/weekly-challenge")
    @Operation(summary = "주간 챌린지 조회")
    public ApiResponse<WeeklyChallengeResDto> getWeeklyChallenge(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal.getUserId();
        return ApiResponse.onSuccess(weeklyChallengeService.getWeeklyChallenge(userId));
    }

    @PostMapping("/weekly-challenge/submit")
    @Operation(summary = "주간 챌린지 제출")
    public ApiResponse<WeeklyChallengeResDto.MyAttemptInfo> submitChallenge(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChallengeSubmitReqDto request
    ) {
        Long userId = principal.getUserId();
        return ApiResponse.onSuccess(weeklyChallengeService.submitChallenge(userId, request));
    }

    @GetMapping("/article")
    @Operation(summary = "기사 기반 퀴즈 조회", description = "특정 기사를 기반으로 퀴즈를 생성하여 반환")
    public ApiResponse<QuizResDto> getArticleQuiz(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String articleId,
            @RequestParam(defaultValue = "3") @Min(1) @Max(10) Integer count
    ) {
        Long userId = principal.getUserId();
        log.info("기사 퀴즈 요청: userId={}, articleId={}, count={}", userId, articleId, count);
        return ApiResponse.onSuccess(quizService.getArticleQuiz(userId, articleId, count));
    }

    /**
     * 특정 용어의 Redis L1 캐시만 삭제 (MongoDB 풀은 유지).
     * 퀴즈 내용 강제 갱신이 필요할 때 사용.
     */
    @DeleteMapping("/cache")
    @Operation(summary = "용어 퀴즈 Redis 캐시 삭제", description = "MongoDB 풀은 유지하고 Redis L1 캐시만 삭제")
    public ApiResponse<String> clearQuizCache(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String term
    ) {
        log.info("용어 퀴즈 캐시 삭제 요청: term={}", term);
        mixedQuizService.clearTermQuizCache(term);
        return ApiResponse.onSuccess("용어 '" + term + "'의 Redis 캐시가 삭제되었습니다.");
    }
}