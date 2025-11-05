package com.example.whiplash.recommend.youtube.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.recommend.youtube.repository.YoutubeRecommendRedisRepository;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeRecommendResponse;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeVideoDTO;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.user.domain.keyword.UserKeyword;
import com.example.whiplash.user.repository.keyword.UserKeywordRepository;
import com.example.whiplash.user.repository.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 유튜브 영상 추천 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LoadYoutubeRecommendService {
	private static final int MINIMUM_RECOMMEND_COUNT = 10;

	private final UserRepository userRepository;
	private final UserKeywordRepository userKeywordRepository;
	private final YoutubeRecommendRedisRepository youtubeRecommendRedisRepository;
	private final YoutubeRecommendTaskProducer youtubeRecommendTaskProducer;

	/**
	 * 인증된 사용자의 키워드 기반 유튜브 영상 추천
	 *
	 * @param currentUserEmail 현재 인증된 사용자 이메일
	 * @return 추천 영상 목록
	 */
	public YoutubeRecommendResponse getKeywordBasedRecommendations(Optional<String> currentUserEmail) {
		// 1. 사용자 인증 확인
		checkUserIsAuthenticated(currentUserEmail);

		// 2. 사용자 조회
		User user = userRepository.findByEmail(currentUserEmail.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		// 3. 사용자 키워드 조회 (우선순위 순)
		List<UserKeyword> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user);

		if (userKeywords.isEmpty()) {
			log.info("User {} has no keywords. Returning common recommendations only.", user.getEmail());
			return getCommonRecommendationsOnly();
		}

		// 4. 키워드 기반 추천 영상 조회
		Set<YoutubeVideo> allRecommendedVideos = new HashSet<>();

		for (UserKeyword userKeyword : userKeywords) {
			List<YoutubeVideo> videosForKeyword = getRecommendedVideosBasedOn(userKeyword);
			allRecommendedVideos.addAll(videosForKeyword);
		}

		int keywordBasedCount = allRecommendedVideos.size();
		log.info("Found {} keyword-based recommendations for user {}", keywordBasedCount, user.getEmail());

		// 5. 최소 개수(10개) 미만이면 공통 추천 영상으로 채우기
		int shortage = MINIMUM_RECOMMEND_COUNT - keywordBasedCount;
		List<YoutubeVideo> commonVideos = new ArrayList<>();

		if (shortage > 0) {
			log.info("Shortage of {} videos. Fetching common recommendations.", shortage);
			Set<YoutubeVideo> allCommonVideos = youtubeRecommendRedisRepository.findCommonRecommendations(shortage);

			// 중복 제거하면서 추가
			for (YoutubeVideo video : allCommonVideos) {
				commonVideos.add(video);

				// 필요한 개수만큼만 추가
				if (commonVideos.size() >= shortage) {
					break;
				}
			}
		}

		// 6. 결과 합치기
		List<YoutubeVideo> allVideos = new ArrayList<>(allRecommendedVideos);
		allVideos.addAll(commonVideos);

		// 7. DTO 변환
		List<YoutubeVideoDTO> videoDTOs = allVideos.stream()
			.map(YoutubeVideoDTO::from)
			.collect(Collectors.toList());

		log.info("Returning {} total videos ({} keyword-based, {} common) for user {}",
			videoDTOs.size(), keywordBasedCount, commonVideos.size(), user.getEmail());

		return YoutubeRecommendResponse.of(videoDTOs, keywordBasedCount, commonVideos.size());
	}

	private List<YoutubeVideo> getRecommendedVideosBasedOn(UserKeyword userKeyword) {
		List<YoutubeVideo> keywordBasedVideos = new ArrayList<>();
		Set<String> uniqueVideoIds = new HashSet<>();

		Long keywordId = userKeyword.getKeyword().getId();

		// Redis에서 해당 키워드의 추천 영상 조회
		List<YoutubeVideo> videosForKeyword = youtubeRecommendRedisRepository.findByKeywordId(keywordId);

		if (videosForKeyword.isEmpty()) {
			// 비동기로 Redis Streams에 키워드 ID 발행
			youtubeRecommendTaskProducer.produceKeywordRecommendTask(keywordId);
		} else {
			// 중복 제거하면서 추가
			for (YoutubeVideo video : videosForKeyword) {
				if (!uniqueVideoIds.contains(video.getVideoId())) {
					keywordBasedVideos.add(video);
					uniqueVideoIds.add(video.getVideoId());
				}
			}
		}

		return keywordBasedVideos;
	}

	/**
	 * 공통 추천 영상만 반환 (키워드가 없는 경우)
	 */
	private YoutubeRecommendResponse getCommonRecommendationsOnly() {
		Set<YoutubeVideo> commonVideos = youtubeRecommendRedisRepository
			.findCommonRecommendations(MINIMUM_RECOMMEND_COUNT);

		List<YoutubeVideoDTO> videoDTOs = commonVideos.stream()
			.map(YoutubeVideoDTO::from)
			.collect(Collectors.toList());

		return YoutubeRecommendResponse.of(videoDTOs, 0, commonVideos.size());
	}

	/**
	 * 사용자 인증 확인
	 */
	private static void checkUserIsAuthenticated(Optional<String> currentUserEmail) {
		if (currentUserEmail.isEmpty()) {
			throw new WhiplashException(ErrorStatus.UNAUTHORIZED);
		}
	}
}
