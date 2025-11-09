package com.example.whiplash.user.service;


import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.converter.InvestorProfileConverter;
import com.example.whiplash.keyword.user.Keyword;
import com.example.whiplash.keyword.user.UserKeyword;
import com.example.whiplash.user.domain.profile.InvestorProfile;
import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.repository.keyword.KeywordRepository;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.web.dto.UserKeywordCreateRequest;
import com.example.whiplash.user.web.dto.request.ProfileRegisterDTO;
import com.example.whiplash.user.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final UserKeywordRepository userKeywordRepository;

    @Transactional
    public User registerProfile(ProfileRegisterDTO profileRegisterDTO, Optional<String> userEmail) {
        checkUserIsAuthenticated(userEmail);

        User user = userRepository.findByEmail(userEmail.get())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        if(user.getUserStatus() != UserStatus.PENDING){
            throw new WhiplashException(ErrorStatus.USER_ALREADY_ACTIVATED);
        }

        user.activateUser();
        user.updateRole(Role.getActiveUserRole());

        InvestorProfile investorProfile = InvestorProfileConverter.toInvestorProfile(profileRegisterDTO, user);
        user.setInvestorProfile(investorProfile);

        return userRepository.save(user);
    }

    @Transactional
    public void createUserKeyword(UserKeywordCreateRequest request, Optional<String> currentUserEmail) {
        checkUserIsAuthenticated(currentUserEmail);
        User user = userRepository.findByEmail(currentUserEmail.get())
                .orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

        Optional<Keyword> optionalKeyword = keywordRepository.findByName(request.keyword());
        if (optionalKeyword.isPresent()) {
            userKeywordRepository.save(UserKeyword.create(user, optionalKeyword.get()));
        } else {
            Keyword keyword = keywordRepository.save(Keyword.create(request.keyword()));
            userKeywordRepository.save(UserKeyword.create(user, keyword));
        }
    }

    private static void checkUserIsAuthenticated(Optional<String> currentUserEmail) {
        if (currentUserEmail.isEmpty()) {
            throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
        }
    }


}
