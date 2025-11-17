package com.example.whiplash.quiz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ChallengeSubmitReqDto {

    @NotNull(message = "챌린지 ID는 필수입니다.")
    private Long challengeId;

    @NotNull(message = "점수는 필수입니다.")
    @Min(value = 0, message = "점수는 0 이상이어야 합니다.")
    private Integer score;

    @NotNull(message = "총 문제 수는 필수입니다.")
    private Integer totalQuestions;

    @NotNull(message = "소요 시간은 필수입니다.")
    @Min(value = 1, message = "소요 시간은 1초 이상이어야 합니다.")
    private Integer timeSpent;  // 초 단위

    @NotEmpty(message = "답안은 필수입니다.")
    private List<Integer> answers;  // 사용자가 선택한 답안 인덱스들
}
