package com.example.whiplash.user.web.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 사용자 키워드 벌크 등록 요청 DTO
 */
public record UserKeywordBulkCreateRequest(
        @NotEmpty(message = "키워드 목록은 필수입니다")
        List<String> keywords
) {
}
