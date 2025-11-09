package com.example.whiplash.term.service;

import com.example.whiplash.term.dto.request.TermAddDto;
import com.example.whiplash.term.entity.Terms;
import com.example.whiplash.term.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Term;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TermService {

    private final TermsRepository termsRepository;

    /**
     * 용어 사전에 새 용어 추가
     */
    public void addTerm(TermAddDto termAddDto, Long userId) {
        Terms terms = Terms.builder()
                .termName(termAddDto.getTermName())
                .AiExplanation(termAddDto.getTermDescription())
                .build();

        termsRepository.save(terms);
    }

    /**
     * 용어 목록 조회
     */
    public List<Term> getTerms() {
        return null;
    }


}
