package com.example.whiplash.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserCreateDTO {
    @NotBlank(message = "회원 이름은 필수정보 입니다.")
    private String username;
    @NotBlank(message = "비밀번호는 필수정보 입니다.")
    private String password;
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private String email;
}
