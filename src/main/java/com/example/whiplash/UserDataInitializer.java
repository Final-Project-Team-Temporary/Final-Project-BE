/*
package com.example.whiplash;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.whiplash.auth.service.AuthService;
import com.example.whiplash.user.web.dto.request.UserCreateDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDataInitializer implements ApplicationRunner {
	private final AuthService authService;


	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("init 시작");
		authService.joinUser(UserCreateDTO.builder()
			.username("gno")
			.email("rmsghchl0@naver.com")
			.password("1111")
			.build());
	}
}
*/
