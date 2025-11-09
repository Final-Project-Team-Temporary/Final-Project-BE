package com.example.whiplash.term.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.term.dto.request.TermAddDto;
import com.example.whiplash.term.service.TermService;
import com.example.whiplash.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terms")
@Tag(name = "용어 관련 API",description = "용어 저장/조회/삭제 API")
public class TermsController {

    private final TermService termService;

    @PostMapping("/explain")
    @Operation(summary = "용어 AI 설명요청", description = "용어에 대한 AI 설명을 요청한다.")
    public ApiResponse<?> getTermsExplain() {
        return null;
    }

    @PostMapping("")
    @Operation(summary = "용어 저장", description = "용어를 나의 용어사전에 저장합니다.")
    public ApiResponse<?> addTerms(@AuthenticationPrincipal User user, @RequestBody TermAddDto termAddDto) {

        termService.addTerm(termAddDto, user.getId());
        return null;
    }

    @GetMapping("")
    @Operation(summary = "용어 리스트 조회", description = "나의 용어사전에 저장된 용어 목록을 조회합니다.")
    public ApiResponse<?> getTerms(){
        return null;
    }

    @GetMapping("/{termsId}")
    @Operation(summary = "용어 상세조회", description = "용어에 대한 상세설명을 조회합니다.")
    public ApiResponse<?> getTermsById(@PathVariable("termsId") String termsId){
        return null;
    }


}
