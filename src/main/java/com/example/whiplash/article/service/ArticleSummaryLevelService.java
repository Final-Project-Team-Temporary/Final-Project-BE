package com.example.whiplash.article.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.repository.user.UserRepository;
import com.example.whiplash.user.web.dto.request.SummaryLevelUpdateRequest;
import com.example.whiplash.user.web.dto.response.SummaryLevelUpdateResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ArticleSummaryLevelService {
	private final UserRepository userRepository;

	@Transactional
	public SummaryLevelUpdateResponse updateSummaryLevel(
		SummaryLevelUpdateRequest request,
		Optional<String> currentUserEmail) {
		checkUserIsAuthenticated(currentUserEmail);

		User user = userRepository.findByEmail(currentUserEmail.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		user.updateSummaryLevel(request.summaryLevel());

		return SummaryLevelUpdateResponse.of(user.getId(), user.getSummaryLevel());
	}

	private static void checkUserIsAuthenticated(Optional<String> currentUserEmail) {
		if (currentUserEmail.isEmpty()) {
			throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
		}
	}


}
