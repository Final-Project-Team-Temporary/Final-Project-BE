package com.example.whiplash.user.web.dto.response;

import com.example.whiplash.article.summary.domain.document.Category;
import com.example.whiplash.user.domain.profile.AgeRange;
import com.example.whiplash.user.domain.profile.InvestmentGoal;
import com.example.whiplash.user.domain.profile.InvestmentLevel;
import com.example.whiplash.user.domain.profile.RiskTolerance;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestorProfileResDto {
    private Long userId;

    private Long id;

    private AgeRange ageRange;

    private InvestmentLevel investmentLevel;

    private InvestmentGoal investmentGoal;

    private RiskTolerance riskTolerance;

}
