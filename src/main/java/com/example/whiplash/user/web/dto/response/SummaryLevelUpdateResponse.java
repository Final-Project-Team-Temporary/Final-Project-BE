package com.example.whiplash.user.web.dto.response;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;

/**
 * 사용자 요약 레벨 업데이트 응답 DTO
 */
public record SummaryLevelUpdateResponse(
        Long userId,
        SummaryLevel summaryLevel
) {
    public static SummaryLevelUpdateResponse of(Long userId, SummaryLevel summaryLevel) {
        return new SummaryLevelUpdateResponse(userId, summaryLevel);
    }
}
