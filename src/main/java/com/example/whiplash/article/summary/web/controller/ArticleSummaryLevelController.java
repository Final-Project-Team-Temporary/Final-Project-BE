package com.example.whiplash.article.summary.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.article.summary.service.ArticleSummaryLevelService;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.user.service.UserService;
import com.example.whiplash.user.web.dto.request.SummaryLevelUpdateRequest;
import com.example.whiplash.user.web.dto.response.SummaryLevelUpdateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/articles")
@RestController
public class ArticleSummaryLevelController {

    private final UserService userService;
    private final ArticleSummaryLevelService articleSummaryLevelService;

    @PutMapping("/summary-level")
    public ApiResponse<SummaryLevelUpdateResponse> updateSummaryLevel(
            @Valid @RequestBody SummaryLevelUpdateRequest request) {

        SummaryLevelUpdateResponse response = articleSummaryLevelService.updateSummaryLevel(
                request,
                SecurityContextUtils.getCurrentUserEmail()
        );

        return ApiResponse.onSuccess(response);
    }
}
