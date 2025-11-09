package com.example.whiplash.term.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
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

    /**
     * 용어 사전에 새 용어 추가
     */
    @Transactional
    public void addTermToDictionary(TermAddDto termAddDto, Optional<Long> userId) {

        checkUserIsAuthenticated(userId);

        User user = userRepository.findById(userId.get())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        // 용어풀에서 기존에 저장되어 있던 용어 조회
        Optional<Terms> terms = termsRepository.findByTermName(termAddDto.getTermName());

        Terms dicTerm = null;

        // 만약 기존에 저장되어 있던 용어가 없다면 -> 용어 풀에 새롭게 저장
        if (terms.isEmpty()) {
            dicTerm = Terms.builder()
                    .termName(termAddDto.getTermName())
                    .AiExplanation(termAddDto.getTermDescription())
                    .build();
            termsRepository.save(dicTerm);
        } else {
            dicTerm = terms.get();
        }

        // 용어사전에 새로운 용어 추가
        UserTerms userTerms = UserTerms.builder()
                .terms(dicTerm)
                .user(user)
                .build();

        userTermsRepository.save(userTerms);
    }

    /**
     * 용어 목록 조회
     */
    public List<DictionaryTermListResDto> getTerms(Optional<Long> userId) {

        checkUserIsAuthenticated(userId);

        List<UserTerms> dicTermList = userTermsRepository.findByUserId(userId.get());


        return dicTermList.stream()
                .map(dicTerm -> DictionaryTermListResDto.builder()
                        .userTermId(dicTerm.getId())
                        .termDescription(dicTerm.getTerms().getAiExplanation())
                        .termName(dicTerm.getTerms().getTermName()).build()).toList();
    }

    private static void checkUserIsAuthenticated(Optional<Long> currentUserId) {
        if (currentUserId.isEmpty()) {
            throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
        }
    }


}
