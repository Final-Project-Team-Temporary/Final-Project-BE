package com.example.whiplash.term.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.quiz.service.QuizPreGenerationService;
import com.example.whiplash.term.dto.request.TermAddDto;
import com.example.whiplash.term.dto.response.DictionaryTermListResDto;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.entity.UserTerms;
import com.example.whiplash.term.repository.TermsRepository;
import com.example.whiplash.term.repository.UserTermsRepository;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.query.Term;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TermService {

    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRepository;
    private final UserRepository userRepository;
    private final QuizPreGenerationService quizPreGenerationService;

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
