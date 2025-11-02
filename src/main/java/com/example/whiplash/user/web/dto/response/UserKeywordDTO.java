package com.example.whiplash.user.web.dto.response;

/**
 * 사용자 키워드 정보 DTO
 */
public record UserKeywordDTO(
        Long userKeywordId,
        String keywordName,
        Integer priority
) {
    public static UserKeywordDTO of(Long userKeywordId, String keywordName, Integer priority) {
        return new UserKeywordDTO(userKeywordId, keywordName, priority);
    }
}
