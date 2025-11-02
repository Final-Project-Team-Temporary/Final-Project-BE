package com.example.whiplash.user.web.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 사용자 키워드 삭제 요청 DTO
 */
public record UserKeywordDeleteRequest(
        @NotEmpty(message = "삭제할 키워드 ID 목록은 필수입니다")
        List<Long> userKeywordIds
) {
}
