package com.example.whiplash.converter;

import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.profile.InvestorProfile;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;

public class InvestorProfileConverter {

    public static InvestorProfile toInvestorProfile(ProfileRegisterDTO profileRegisterDTO, User user) {
        return InvestorProfile.create(
                user,
                profileRegisterDTO.getAgeRange(),
                profileRegisterDTO.getInvestmentLevel(),
                profileRegisterDTO.getInvestmentGoal(),
                profileRegisterDTO.getRiskTolerance(),
                profileRegisterDTO.getInterestCategories()
        );
    }
}
