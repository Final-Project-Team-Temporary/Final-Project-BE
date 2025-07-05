package com.example.whiplash.user.service;


import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.example.whiplash.converter.AuthConverter;
import com.example.whiplash.converter.InvestorProfileConverter;
import com.example.whiplash.converter.UserConverter;
import com.example.whiplash.domain.entity.profile.InvestorProfile;
import com.example.whiplash.domain.repository.InvestorProfileRepository;
import com.example.whiplash.user.Role;
import com.example.whiplash.user.User;
import com.example.whiplash.user.UserModifyRequestDTO;
import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.AuthResponse;
import com.example.whiplash.user.dto.ProfileRegisterDTO;
import com.example.whiplash.user.dto.UserCreateDTO;
import com.example.whiplash.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


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
