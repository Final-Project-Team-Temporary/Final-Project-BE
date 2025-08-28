package com.example.whiplash.user.service;


import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.converter.InvestorProfileConverter;
import com.example.whiplash.user.domain.profile.InvestorProfile;
import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User registerProfile(ProfileRegisterDTO profileRegisterDTO, String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        if(user.getUserStatus() != UserStatus.PENDING){
            throw new WhiplashException(ErrorStatus.USER_ALREADY_ACTIVATED);
        }

        user.activateUser();
        user.updateRole(Role.getActiveUserRole());

        InvestorProfile investorProfile = InvestorProfileConverter.toInvestorProfile(profileRegisterDTO);

        user.setInvestorProfile(investorProfile);

        return userRepository.save(user);
    }



}
