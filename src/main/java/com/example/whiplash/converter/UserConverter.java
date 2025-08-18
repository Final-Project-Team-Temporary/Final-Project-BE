package com.example.whiplash.converter;

import com.example.whiplash.user.Role;
import com.example.whiplash.user.SocialProvider;
import com.example.whiplash.user.User;
import com.example.whiplash.user.UserStatus;
import com.example.whiplash.user.dto.KakaoUserInfoResponseDTO;
import com.example.whiplash.user.dto.UserCreateDTO;

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
