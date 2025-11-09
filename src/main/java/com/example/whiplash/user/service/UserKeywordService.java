package com.example.whiplash.user.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.keyword.Keyword;
import com.example.whiplash.user.domain.keyword.UserKeyword;
import com.example.whiplash.user.repository.keyword.KeywordRepository;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.UserKeywordBulkCreateRequest;
import com.example.whiplash.user.web.dto.request.UserKeywordDeleteRequest;
import com.example.whiplash.user.web.dto.response.UserKeywordBulkCreateResponse;
import com.example.whiplash.user.web.dto.response.UserKeywordDTO;
import com.example.whiplash.user.web.dto.response.UserKeywordListResponse;

import lombok.RequiredArgsConstructor;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserKeywordService {
	private final UserRepository userRepository;
	private final KeywordRepository keywordRepository;
	private final UserKeywordRepository userKeywordRepository;
	private final UserService userService;

	public UserKeywordListResponse getUserKeywords(Optional<Long> currentUserId) {
		checkUserIsAuthenticated(currentUserId);

		log.info("current user: {}", currentUserId.get());

		User user = userRepository.findById(currentUserId.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		List<UserKeyword> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user);

		log.info("user keywords: {}", userKeywords.size());

		List<UserKeywordDTO> keywordDTOs = userKeywords.stream()
			.map(uk -> UserKeywordDTO.of(
				uk.getId(),
				uk.getKeywordName(),
				uk.getPriority()
			))
			.toList();

		return UserKeywordListResponse.of(keywordDTOs);
	}

	@Transactional
	public UserKeywordBulkCreateResponse createUserKeywords(
		UserKeywordBulkCreateRequest request,
		Optional<Long> currentUserId) {
		checkUserIsAuthenticated(currentUserId);

		User user = userRepository.findById(currentUserId.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		List<UserKeyword> createdKeywords = new ArrayList<>();

		for (String keywordName : request.keywords()) {
			Optional<Keyword> optionalKeyword = keywordRepository.findByName(keywordName);
			UserKeyword userKeyword;

			if (optionalKeyword.isPresent()) {
				userKeyword = userKeywordRepository.save(UserKeyword.create(user, optionalKeyword.get()));
			} else {
				Keyword keyword = keywordRepository.save(Keyword.create(keywordName));
				userKeyword = userKeywordRepository.save(UserKeyword.create(user, keyword));
			}

			createdKeywords.add(userKeyword);
		}

		List<UserKeywordDTO> keywordDTOs = createdKeywords.stream()
			.map(uk -> UserKeywordDTO.of(
				uk.getId(),
				uk.getKeywordName(),
				uk.getPriority()
			))
			.toList();

		return UserKeywordBulkCreateResponse.of(createdKeywords.size(), keywordDTOs);
	}

	@Transactional
	public void deleteUserKeywords(
		UserKeywordDeleteRequest request,
		Optional<Long> currentUserId) {
		checkUserIsAuthenticated(currentUserId);

		User user = userRepository.findById(currentUserId.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		List<UserKeyword> userKeywords = userKeywordRepository.findAllById(request.userKeywordIds());

		// 요청된 UserKeyword가 현재 사용자의 것인지 검증
		checkUserIsOwnerOfUserKeywords(userKeywords, user);

		userKeywordRepository.deleteAll(userKeywords);
	}

	private static void checkUserIsOwnerOfUserKeywords(List<UserKeyword> userKeywords, User user) {
		for (UserKeyword userKeyword : userKeywords) {
			if (!userKeyword.getUser().getId().equals(user.getId())) {
				throw new WhiplashException(ErrorStatus.FORBIDDEN);
			}
		}
	}

	private static void checkUserIsAuthenticated(Optional<Long> currentUserId) {
		if (currentUserId.isEmpty()) {
			throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
		}
	}


}
