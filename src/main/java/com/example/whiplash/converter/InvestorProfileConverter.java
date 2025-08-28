package com.example.whiplash.converter;

import com.example.whiplash.user.domain.profile.InvestorProfile;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;

public class InvestorProfileConverter {

    public static InvestorProfile toInvestorProfile(ProfileRegisterDTO profileRegisterDTO) {
        return InvestorProfile.builder()
                .ageRange(profileRegisterDTO.getAgeRange())
                .investmentLevel(profileRegisterDTO.getInvestmentLevel())
                .investmentGoal(profileRegisterDTO.getInvestmentGoal())
                .riskTolerance(profileRegisterDTO.getRiskTolerance())
                .build();
    }
}
