package com.example.whiplash.quiz.service;

import com.example.whiplash.quiz.dto.TermScore;
import com.example.whiplash.quiz.dto.request.MixedQuizReqDto;
import com.example.whiplash.quiz.dto.request.SmartMixReqDto;
import com.example.whiplash.quiz.dto.response.MixedQuizResDto;
import com.example.whiplash.quiz.repository.QuizResultRepository;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.UserTermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmartMixService {

    private final UserTermsRepository userTermsRepository;
    private final QuizResultRepository quizResultRepository;
    private final MixedQuizService mixedQuizService;

    /**
     * 스마트 랜덤 모의고사 생성
     */
    @Transactional(readOnly = true)
    public MixedQuizResDto createSmartMixQuiz(Long userId, SmartMixReqDto request) {
        log.info("스마트 랜덤 모의고사 생성: userId={}, totalQuestions={}",
                userId, request.getTotalQuestions());

        // 1. 사용자가 저장한 모든 용어 조회
        List<UserTerms> userTermsList = userTermsRepository.findByUserId(userId);

        if (userTermsList.isEmpty()) {
            throw new RuntimeException("저장된 용어가 없습니다.");
        }

        // 2. 각 용어별 점수 계산
        List<TermScore> termScores = calculateTermScores(userId, userTermsList);

        // 3. 점수 순으로 정렬 (높은 순)
        Collections.sort(termScores);

        // 4. 상위 용어 선택 (최소 3개, 최대 totalQuestions/2개)
        int numTerms = Math.min(
                Math.max(3, request.getTotalQuestions() / 2),
                Math.min(10, termScores.size())
        );

        List<String> selectedTerms = termScores.stream()
                .limit(numTerms)
                .map(TermScore::getTermName)
                .collect(Collectors.toList());

        log.info("선정된 용어: {}", selectedTerms);

        // 5. 각 용어별 문제 수 배분
        Map<String, Integer> distribution = distributeQuestions(
                selectedTerms,
                request.getTotalQuestions()
        );

        // 6. MixedQuizService를 활용해서 퀴즈 생성
        MixedQuizReqDto mixedRequest = new MixedQuizReqDto();
        mixedRequest.setTerms(selectedTerms);
        mixedRequest.setQuestionsPerTerm(
                request.getTotalQuestions() / selectedTerms.size()
        );

        MixedQuizResDto response = mixedQuizService.createMixedQuiz(userId, mixedRequest);

        // 7. 선정 이유 추가 (로그용)
        logSelectionReason(termScores, selectedTerms);

        return response;
    }

    /**
     * 각 용어별 점수 계산 (가중치 기반)
     */
    private List<TermScore> calculateTermScores(Long userId, List<UserTerms> userTermsList) {
        List<TermScore> termScores = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        for (UserTerms userTerm : userTermsList) {
            String termName = userTerm.getTerms().getTermName();
            double score = 0.0;

            // 1. 최근 저장 여부 (7일 이내) → 가중치 50
            if (userTerm.getCreatedAt().isAfter(sevenDaysAgo)) {
                score += 50.0;
            }

            // 2. 정답률 낮은 용어 → 가중치 30
            Double avgAccuracy = quizResultRepository
                    .findAverageAccuracyByUserIdAndTerm(userId, termName);

            if (avgAccuracy != null && avgAccuracy < 0.7) {
                score += 30.0 * (1.0 - avgAccuracy);  // 정답률 낮을수록 높은 점수
            }

            // 3. 오래 안 푼 용어 → 가중치 20
            LocalDateTime lastSolved = quizResultRepository
                    .findLastSolvedAtByUserIdAndTerm(userId, termName);

            if (lastSolved == null) {
                // 한 번도 안 풀었으면 최고 점수
                score += 20.0;
            } else if (lastSolved.isBefore(sevenDaysAgo)) {
                // 7일 이상 안 풀었으면 가중치 부여
                long daysSinceLastSolved = java.time.Duration
                        .between(lastSolved, now)
                        .toDays();
                score += Math.min(20.0, daysSinceLastSolved * 2.0);
            }

            termScores.add(new TermScore(
                    termName,
                    score,
                    lastSolved,
                    avgAccuracy
            ));
        }

        return termScores;
    }

    /**
     * 총 문제 수를 용어별로 배분
     */
    private Map<String, Integer> distributeQuestions(
            List<String> terms,
            int totalQuestions
    ) {
        Map<String, Integer> distribution = new HashMap<>();

        int questionsPerTerm = totalQuestions / terms.size();
        int remainder = totalQuestions % terms.size();

        for (int i = 0; i < terms.size(); i++) {
            int count = questionsPerTerm;
            if (i < remainder) {
                count++;  // 나머지를 앞쪽 용어에 분배
            }
            distribution.put(terms.get(i), count);
        }

        return distribution;
    }

    /**
     * 선정 이유 로깅
     */
    private void logSelectionReason(List<TermScore> allScores, List<String> selected) {
        log.info("=== 스마트 용어 선정 결과 ===");

        for (TermScore score : allScores) {
            if (selected.contains(score.getTermName())) {
                log.info("✅ {}: 점수={}, 정답률={}, 마지막 풀이={}",
                        score.getTermName(),
                        String.format("%.1f", score.getScore()),
                        score.getAccuracy() != null
                                ? String.format("%.0f%%", score.getAccuracy() * 100)
                                : "N/A",
                        score.getLastSolvedAt() != null
                                ? score.getLastSolvedAt()
                                : "한번도 안 풀음"
                );
            }
        }
    }
}
