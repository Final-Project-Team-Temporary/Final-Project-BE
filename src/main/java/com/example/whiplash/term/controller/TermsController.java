package com.example.whiplash.term.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.config.security.UserPrincipal;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.term.dto.request.TermAddDto;
import com.example.whiplash.term.dto.response.DictionaryTermListResDto;
import com.example.whiplash.term.dto.response.TermSuggestionResponse;
import com.example.whiplash.term.service.TermService;
import com.example.whiplash.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Term;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/terms")
@Tag(name = "용어 관련 API", description = "용어 저장/조회/삭제 API")
public class TermsController {

    private final TermService termService;

    @PostMapping("")
    @Operation(summary = "용어 저장", description = "용어를 나의 용어사전에 저장합니다.")
    public ApiResponse<?> addTerms(@RequestBody TermAddDto termAddDto, @AuthenticationPrincipal UserPrincipal principal) {

        Long userId = principal.getUserId();

        termService.addTermToDictionary(termAddDto, userId);

        return ApiResponse.onSuccess(null);
    }

    @GetMapping("")
    @Operation(summary = "용어 리스트 조회", description = "나의 용어사전에 저장된 용어 목록을 조회합니다.")
    public ApiResponse<?> getTerms(@AuthenticationPrincipal UserPrincipal principal,
                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        Long userId = principal.getUserId();

        Pageable pageRequest = PageRequest.of(page, size);

        Page<DictionaryTermListResDto> dicTermListResDto = termService.getTerms(userId, pageRequest);

        return ApiResponse.onSuccess(dicTermListResDto);
    }

    @GetMapping("/search")
    public ApiResponse<?> searchTerms(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Long userId = principal.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        Page<DictionaryTermListResDto> response = termService.searchTerms(userId, keyword, pageable);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/search/suggestions")
    public ApiResponse<?> searchSuggestions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String keyword
    ) {

        Long userId = principal.getUserId();

        TermSuggestionResponse response = termService.getSuggestions(userId, keyword);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{termsId}")
    @Operation(summary = "용어 상세조회", description = "용어에 대한 상세설명을 조회합니다.")
    public ApiResponse<?> getTermsById(@PathVariable("termsId") String termsId) {
        return null;
    }

    @GetMapping("/explain")
    @Operation(summary = "용어 AI설명 요청", description = "용어에 대한 AI 설명을 요청합니다.")
    public ApiResponse<?> getTermsExplain(@RequestParam String term) {
        return ApiResponse.onSuccess(termService.getTermExplanation(term));
    }

    @DeleteMapping("/{userTermsId}")
    public ApiResponse<?> deleteTerms(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long userTermsId
    ) {
        Long userId = principal.getUserId();

        termService.deleteTerm(userId, userTermsId);

        return ApiResponse.onSuccess(null);
    }
}
