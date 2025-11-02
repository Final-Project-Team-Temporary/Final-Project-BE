package com.example.whiplash.user.web.dto.request;

import com.example.whiplash.domain.entity.history.email.SummaryLevel;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 요약 레벨 업데이트 요청 DTO
 */
public record SummaryLevelUpdateRequest(
        @NotNull(message = "요약 레벨은 필수입니다")
        SummaryLevel summaryLevel
) {
}
