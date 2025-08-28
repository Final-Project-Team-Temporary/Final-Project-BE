package com.example.whiplash.user.web.dto.request;

import com.example.whiplash.user.domain.profile.AgeRange;
import com.example.whiplash.user.domain.profile.InvestmentGoal;
import com.example.whiplash.user.domain.profile.InvestmentLevel;
import com.example.whiplash.user.domain.profile.RiskTolerance;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
@Builder
@Getter
public class ProfileRegisterDTO {
    @NotNull(message = "연령대는 필수입니다")
    private AgeRange ageRange;

    @NotNull(message = "투자 경험은 필수입니다")
    private InvestmentLevel investmentLevel;

    @NotNull(message = "위험 성향은 필수입니다")
    private RiskTolerance riskTolerance;

    @NotNull(message = "투자 목적은 필수입니다")
    private InvestmentGoal investmentGoal;

}
