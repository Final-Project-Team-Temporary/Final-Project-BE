package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.dto.request.ChallengeSubmitReqDto;
import com.example.whiplash.quiz.dto.request.MixedQuizReqDto;
import com.example.whiplash.quiz.dto.response.MixedQuizResDto;
import com.example.whiplash.quiz.dto.response.WeeklyChallengeResDto;
import com.example.whiplash.quiz.entity.ChallengeAttempt;
import com.example.whiplash.quiz.entity.WeeklyChallenge;
import com.example.whiplash.quiz.repository.ChallengeAttemptRepository;
import com.example.whiplash.quiz.repository.WeeklyChallengeRepository;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyChallengeService {

    private final WeeklyChallengeRepository weeklyChallengeRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final UserTermsRepository userTermsRepository;
    private final MixedQuizService mixedQuizService;
    private final ObjectMapper objectMapper;

    /**
     * 이번 주 챌린지 조회 (없으면 자동 생성)
     */
    @Transactional
    public WeeklyChallengeResDto getWeeklyChallenge(Long userId) {
        log.info("주간 챌린지 조회: userId={}", userId);

        // 1. 이번 주 월요일 계산
        LocalDate monday = getThisWeekMonday();

        // 2. 챌린지 조회 or 생성
        WeeklyChallenge challenge = weeklyChallengeRepository
                .findByWeekStartDate(monday)
                .orElseGet(() -> generateWeeklyChallenge(monday, userId));

        // 3. 사용자의 도전 여부 확인
        Optional<ChallengeAttempt> attemptOpt = challengeAttemptRepository
                .findByUserIdAndChallengeId(userId, challenge.getId());

        log.info("weekly challenge : {}", challenge.getQuizzesJson());

        // 4. 퀴즈 데이터 (이미 도전했으면 null)
        List<MixedQuizResDto.QuizWithTerm> quizzes = attemptOpt.isPresent()
                ? null
                : parseQuizzesFromJson(challenge.getQuizzesJson());

        // 5. 랭킹 조회 (상위 10명)
        List<WeeklyChallengeResDto.RankingEntry> ranking = getRanking(challenge.getId());

        // 6. 내 도전 정보
        WeeklyChallengeResDto.MyAttemptInfo myAttempt = null;
        if (attemptOpt.isPresent()) {
            ChallengeAttempt attempt = attemptOpt.get();
            int rank = challengeAttemptRepository.calculateRank(
                    challenge.getId(),
                    attempt.getScore(),
                    attempt.getTimeSpent()
            );
            myAttempt = WeeklyChallengeResDto.MyAttemptInfo.from(attempt, rank);
        }

        // 7. 통계
        long totalParticipants = challengeAttemptRepository.countByChallengeId(challenge.getId());
        Double averageScore = calculateAverageScore(challenge.getId());

        WeeklyChallengeResDto.StatsInfo stats = new WeeklyChallengeResDto.StatsInfo(
                totalParticipants,
                averageScore
        );

        // 8. 용어 목록 파싱
        String termsStr = parseTermsFromJson(challenge.getTermsJson());

        return new WeeklyChallengeResDto(
                WeeklyChallengeResDto.ChallengeInfo.from(challenge, termsStr),
                myAttempt,
                quizzes,
                ranking,
                stats
        );
    }

    /**
     * 챌린지 제출
     */
    @Transactional
    public WeeklyChallengeResDto.MyAttemptInfo submitChallenge(
            Long userId,
            ChallengeSubmitReqDto request
    ) {
        log.info("챌린지 제출: userId={}, challengeId={}, score={}",
                userId, request.getChallengeId(), request.getScore());

        // 1. 중복 제출 확인
        Optional<ChallengeAttempt> existing = challengeAttemptRepository
                .findByUserIdAndChallengeId(userId, request.getChallengeId());

        if (existing.isPresent()) {
            throw new RuntimeException("이미 도전한 챌린지입니다.");
        }

        // 2. 챌린지 존재 확인
        WeeklyChallenge challenge = weeklyChallengeRepository
                .findById(request.getChallengeId())
                .orElseThrow(() -> new RuntimeException("챌린지를 찾을 수 없습니다."));

        // 3. 제출 기한 확인
        if (!challenge.isActive()) {
            throw new RuntimeException("챌린지 기간이 아닙니다.");
        }

        // 4. 도전 기록 저장
        ChallengeAttempt attempt = ChallengeAttempt.builder()
                .userId(userId)
                .challengeId(request.getChallengeId())
                .score(request.getScore())
                .totalQuestions(request.getTotalQuestions())
                .timeSpent(request.getTimeSpent())
                .build();

        challengeAttemptRepository.save(attempt);

        // 5. 순위 계산
        int rank = challengeAttemptRepository.calculateRank(
                request.getChallengeId(),
                request.getScore(),
                request.getTimeSpent()
        );

        log.info("챌린지 제출 완료: userId={}, rank={}", userId, rank);

        return WeeklyChallengeResDto.MyAttemptInfo.from(attempt, rank);
    }

    /**
     * 주간 챌린지 생성 (지난주 용어 기반)
     */
    private WeeklyChallenge generateWeeklyChallenge(LocalDate monday, Long sampleUserId) {
        log.info("주간 챌린지 생성: weekStart={}", monday);

        LocalDate sunday = monday.plusDays(6);
        LocalDate lastWeekStart = monday.minusDays(7);
        LocalDate lastWeekEnd = monday.minusDays(1);

        // 샘플 사용자의 지난주 저장 용어 조회
        // (실제로는 모든 사용자의 공통 용어 or 인기 용어 사용)
        List<UserTerms> lastWeekTerms = userTermsRepository
                .findByUserIdAndCreatedAtBetween(
                        sampleUserId,
                        lastWeekStart.atStartOfDay(),
                        lastWeekEnd.atTime(23, 59, 59)
                );

        if (lastWeekTerms.isEmpty()) {
            // 지난주 용어가 없으면 전체 용어에서 랜덤 선택
            lastWeekTerms = userTermsRepository.findByUserId(sampleUserId);
        }

        // 최대 12개 용어 선택
        List<String> selectedTerms = lastWeekTerms.stream()
                .map(ut -> ut.getTerms().getTermName())
                .distinct()
                .limit(12)
                .collect(Collectors.toList());

        if (selectedTerms.isEmpty()) {
            throw new RuntimeException("챌린지를 생성할 용어가 없습니다.");
        }

        // 퀴즈 생성 (각 용어당 1개)
        MixedQuizReqDto request = new MixedQuizReqDto();
        request.setTerms(selectedTerms);
        request.setQuestionsPerTerm(1);

        MixedQuizResDto quizResponse = mixedQuizService.createMixedQuiz(sampleUserId, request);

        // JSON 변환
        String termsJson = convertToJson(selectedTerms);
        String quizzesJson = convertToJson(quizResponse.getQuizzes());

        // 챌린지 저장
        WeeklyChallenge challenge = WeeklyChallenge.builder()
                .weekStartDate(monday)
                .weekEndDate(sunday)
                .totalQuestions(quizResponse.getTotalQuestions())
                .timeLimit(10)  // 10분
                .termsJson(termsJson)
                .quizzesJson(quizzesJson)
                .build();

        WeeklyChallenge saved = weeklyChallengeRepository.save(challenge);

        log.info("주간 챌린지 생성 완료: id={}, terms={}, questions={}",
                saved.getId(), selectedTerms.size(), saved.getTotalQuestions());

        return saved;
    }

    /**
     * 이번 주 월요일 날짜 계산
     */
    private LocalDate getThisWeekMonday() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY);
    }

    /**
     * 랭킹 조회 (상위 10명)
     */
    private List<WeeklyChallengeResDto.RankingEntry> getRanking(Long challengeId) {
        List<ChallengeAttempt> top10 = challengeAttemptRepository
                .findTop10ByChallengeIdOrderByScoreDesc(challengeId);

        List<WeeklyChallengeResDto.RankingEntry> ranking = new ArrayList<>();
        for (int i = 0; i < top10.size(); i++) {
            ChallengeAttempt attempt = top10.get(i);
            ranking.add(new WeeklyChallengeResDto.RankingEntry(
                    i + 1,  // 순위
                    attempt.getUserId(),
                    "사용자" + attempt.getUserId(),  // 향후 실제 닉네임으로 교체
                    attempt.getScore(),
                    attempt.getTotalQuestions(),
                    attempt.getTimeSpent(),
                    attempt.getAccuracy()
            ));
        }

        return ranking;
    }

    /**
     * 평균 점수 계산
     */
    private Double calculateAverageScore(Long challengeId) {
        List<ChallengeAttempt> attempts = challengeAttemptRepository
                .findTop10ByChallengeIdOrderByScoreDesc(challengeId);

        if (attempts.isEmpty()) {
            return 0.0;
        }

        double sum = attempts.stream()
                .mapToInt(ChallengeAttempt::getScore)
                .sum();

        return sum / attempts.size();
    }

    /**
     * JSON 변환 헬퍼
     */
    private String convertToJson(Object obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            log.debug("JSON 변환 성공: type={}, length={}", obj.getClass().getSimpleName(), json.length());
            return json;
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 실패: obj={}", obj, e);
            throw new RuntimeException("JSON 변환 실패: " + e.getMessage(), e);
        }
    }

    private List<MixedQuizResDto.QuizWithTerm> parseQuizzesFromJson(String json) {
        try {
            log.debug("JSON 파싱 시도: json={}", json);
            List<MixedQuizResDto.QuizWithTerm> result = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(
                            List.class,
                            MixedQuizResDto.QuizWithTerm.class
                    )
            );
            log.debug("JSON 파싱 성공: size={}", result.size());
            return result;
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패 - json 내용: {}", json, e);
            log.error("에러 상세: {}", e.getMessage());
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String parseTermsFromJson(String json) {
        try {
            List<String> terms = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            return String.join(", ", terms);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
