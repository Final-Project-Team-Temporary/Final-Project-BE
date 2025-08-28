package com.example.whiplash.converter;

import com.example.whiplash.user.domain.Role;
import com.example.whiplash.user.domain.SocialProvider;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.UserStatus;
import com.example.whiplash.user.web.dto.response.KakaoUserInfoResponseDTO;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;

public class UserConverter {

    public static User toUser(UserCreateDTO userCreateDTO) {
        return User.builder()
                .email(userCreateDTO.getEmail())
                .name(userCreateDTO.getUsername())
                .userStatus(UserStatus.PENDING)
                .role(Role.getDefaultRole())
                .build();
    }

    public static User toKakaoUser(KakaoUserInfoResponseDTO userInfo) {
        return User.builder()
                .kakaoId(userInfo.getId())
                .name(userInfo.getKakaoAccount().getName())
                .userStatus(UserStatus.PENDING)
                .role(Role.getDefaultRole())
                .socialProvider(SocialProvider.KAKAO)
                .build();
    }
}
