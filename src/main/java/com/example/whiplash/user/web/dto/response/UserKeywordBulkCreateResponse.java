package com.example.whiplash.user.web.dto.response;

import java.util.List;

/**
 * 사용자 키워드 벌크 등록 응답 DTO
 */
public record UserKeywordBulkCreateResponse(
        int createdCount,
        List<UserKeywordDTO> keywords
) {
    public static UserKeywordBulkCreateResponse of(int createdCount, List<UserKeywordDTO> keywords) {
        return new UserKeywordBulkCreateResponse(createdCount, keywords);
    }
}
