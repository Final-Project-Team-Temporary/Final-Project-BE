package com.example.whiplash.term.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.quiz.client.AiServerClient;
import com.example.whiplash.quiz.service.QuizPreGenerationService;
import com.example.whiplash.term.dto.request.TermAddDto;
import com.example.whiplash.term.dto.response.DictionaryTermListResDto;
import com.example.whiplash.term.dto.response.TermExplainResDto;
import com.example.whiplash.term.dto.response.TermSuggestionResponse;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.TermsRepository;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Term;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermService {

    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserRepository userRepository;
    private final QuizPreGenerationService quizPreGenerationService;
    private final AiServerClient aiServerClient;

    /**
     * 용어 사전에 새 용어 추가
     */
    @Transactional
    public void addTermToDictionary(TermAddDto termAddDto, Long userId) {


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        // Terms 저장 로직 (개선된 버전)
        Terms dicTerm = getOrCreateTerm(termAddDto);

        // ⭐ 중복 체크 추가
        boolean alreadyExists = userTermsRepository.existsByUserAndTerms(user, dicTerm);
        if (alreadyExists) {
            throw new WhiplashException(ErrorStatus.TERM_ALREADY_IN_DICTIONARY);
            // 또는 조용히 리턴: return;
        }

        // 용어사전에 새로운 용어 추가
        UserTerms userTerms = UserTerms.builder()
                .terms(dicTerm)
                .user(user)
                .build();

        userTermsRepository.save(userTerms);

        // 비동기 퀴즈 생성 요청
        quizPreGenerationService.generateQuizAsync(userId, dicTerm.getTermName());
    }

    /**
     * 용어 목록 조회
     */
    public List<DictionaryTermListResDto> getTerms(Long userId) {

        List<UserTerms> dicTermList = userTermsRepository.findByUserId(userId);

        return dicTermList.stream()
                .map(dicTerm -> DictionaryTermListResDto.builder()
                        .userTermId(dicTerm.getId())
                        .termDescription(dicTerm.getTerms().getAiExplanation())
                        .termName(dicTerm.getTerms().getTermName())
                        .createdAt(dicTerm.getTerms().getCreatedAt())
                        .build()).toList();
    }

    /**
     * 용어 AI 설명요청
     */
    public TermExplainResDto getTermExplanation(String term) {
        Optional<Terms> findTerm = termsRepository.findByTermName(term);

        if (findTerm.isPresent()) {
            Terms terms = findTerm.get();
            return new TermExplainResDto(terms.getTermName(), terms.getAiExplanation());
        }

        return aiServerClient.getTermExplain(term);
    }

    /**
     * 용어사전에서 용어 삭제
     */
    @Transactional
    public void deleteTerm(Long userId, Long userTermsId) {

        UserTerms userTerms = userTermsRepository.findById(userTermsId)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.TERM_NOT_FOUND));

        if (!userTerms.getUser().getId().equals(userId)) {
            throw new WhiplashException(ErrorStatus.NOT_YOUR_DICTIONARY_TERM);
        }

        userTermsRepository.deleteById(userTermsId);
    }

    /**
     * 용어 검색
     *
     * @param userId
     * @param keyword
     * @param pageable
     * @return
     */
    public Page<DictionaryTermListResDto> searchTerms(Long userId, String keyword, Pageable pageable) {
        log.info("🔍 용어 검색: userId={}, keyword={}", userId, keyword);

        // 1. 키워드 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("⚠️ 검색 키워드 없음");
            throw new WhiplashException(ErrorStatus.INVALID_SEARCH_KEYWORD);
        }

        // 2. 키워드 전처리 (공백 제거, 소문자 변환)
        String sanitizedKeyword = keyword.trim();

        // 3. 검색 실행
        Page<UserTerms> terms = userTermsRepository.searchByTermContaining(
                userId,
                sanitizedKeyword,
                pageable
        );

        Page<DictionaryTermListResDto> response = terms.map(term ->
                DictionaryTermListResDto.builder()
                        .userTermId(term.getId())
                        .termName(term.getTerms().getTermName())
                        .termDescription(term.getTerms().getAiExplanation())
                        .createdAt(term.getTerms().getCreatedAt())
                        .build());

        log.info("✅ 용어 검색 완료: totalElements={}", response.getTotalElements());


        return response;
    }

    /**
     * ⭐ 용어 자동완성 제안
     */
    @Transactional(readOnly = true)
    public TermSuggestionResponse getSuggestions(Long userId, String keyword) {
        log.info("💡 자동완성 요청: userId={}, keyword={}", userId, keyword);

        // 1. 키워드 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            return TermSuggestionResponse.builder()
                    .suggestions(List.of())
                    .build();
        }

        // 2. 키워드 전처리
        String sanitizedKeyword = keyword.trim();

        // 3. 최대 10개 제안
        Pageable pageable = PageRequest.of(0, 10);

        List<String> suggestions = userTermsRepository.findTermSuggestions(
                userId,
                sanitizedKeyword,
                pageable
        );

        log.info("✅ 자동완성 완료: count={}", suggestions.size());

        return TermSuggestionResponse.builder()
                .suggestions(suggestions)
                .build();
    }


    // Terms 조회/생성 로직 분리 (가독성 향상)
    private Terms getOrCreateTerm(TermAddDto termAddDto) {
        try {
            Optional<Terms> terms = termsRepository.findByTermName(termAddDto.getTermName());

            if (terms.isEmpty()) {
                Terms newTerm = Terms.builder()
                        .termName(termAddDto.getTermName())
                        .AiExplanation(termAddDto.getTermDescription())
                        .build();
                return termsRepository.save(newTerm);
            }
            return terms.get();

        } catch (DataIntegrityViolationException e) {
            return termsRepository.findByTermName(termAddDto.getTermName())
                    .orElseThrow(() -> new WhiplashException(ErrorStatus.TERM_SAVE_FAILED));
        }
    }


}
