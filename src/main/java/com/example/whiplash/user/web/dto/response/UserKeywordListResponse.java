package com.example.whiplash.user.web.dto.response;

import java.util.List;

/**
 * 사용자 키워드 목록 조회 응답 DTO
 */
public record UserKeywordListResponse(
        List<UserKeywordDTO> keywords
) {
    public static UserKeywordListResponse of(List<UserKeywordDTO> keywords) {
        return new UserKeywordListResponse(keywords);
    }
}
