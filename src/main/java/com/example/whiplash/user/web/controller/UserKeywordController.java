package com.example.whiplash.user.web.controller;

import com.example.whiplash.apiPayload.ApiResponse;
import com.example.whiplash.global.util.SecurityContextUtils;
import com.example.whiplash.user.service.UserKeywordService;
import com.example.whiplash.user.web.dto.request.UserKeywordBulkCreateRequest;
import com.example.whiplash.user.web.dto.request.UserKeywordDeleteRequest;
import com.example.whiplash.user.web.dto.response.UserKeywordBulkCreateResponse;
import com.example.whiplash.user.web.dto.response.UserKeywordListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users/keywords")
@RestController
public class UserKeywordController {

	private final UserKeywordService userKeywordService;

	/**
	 * 사용자의 키워드 조회
	 */
	@GetMapping
	public ApiResponse<UserKeywordListResponse> getUserKeywords() {
		UserKeywordListResponse response = userKeywordService.getUserKeywords(
				SecurityContextUtils.getCurrentUserId()
		);
		return ApiResponse.onSuccess(response);
	}

	/**
	 * 사용자의 키워드 벌크 등록
	 */
	@PostMapping
	public ApiResponse<UserKeywordBulkCreateResponse> createUserKeywords(
			@Valid @RequestBody UserKeywordBulkCreateRequest request) {

		UserKeywordBulkCreateResponse response = userKeywordService.createUserKeywords(
				request,
				SecurityContextUtils.getCurrentUserId()
		);

		return ApiResponse.onCreated(response);
	}

	/**
	 * 사용자의 키워드 벌크 삭제
	 */
	@DeleteMapping
	public ApiResponse<Void> deleteUserKeywords(
			@Valid @RequestBody UserKeywordDeleteRequest request) {

		log.info("사용자 키워드 벌크 삭제 요청: count={}", request.userKeywordIds().size());

		userKeywordService.deleteUserKeywords(
				request,
				SecurityContextUtils.getCurrentUserId()
		);

		log.info("사용자 키워드 벌크 삭제 완료");

		return ApiResponse.onSuccess(null);
	}
}
