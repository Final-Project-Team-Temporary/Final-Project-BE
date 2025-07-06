package com.example.whiplash.converter;

import com.example.whiplash.domain.entity.profile.InvestorProfile;
import com.example.whiplash.user.dto.ProfileRegisterDTO;

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
