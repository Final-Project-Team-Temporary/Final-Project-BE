package com.example.whiplash.recommend.youtube.service;

import com.example.whiplash.apiPayload.ErrorStatus;
import com.example.whiplash.apiPayload.exception.WhiplashException;
import com.example.whiplash.recommend.youtube.domain.YoutubeVideo;
import com.example.whiplash.recommend.youtube.repository.YoutubeRecommendRedisRepository;
import com.example.whiplash.recommend.youtube.streams.producer.YoutubeRecommendTaskProducer;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeRecommendResponse;
import com.example.whiplash.recommend.youtube.web.dto.response.YoutubeVideoDTO;
import com.example.whiplash.user.domain.User;
import com.example.whiplash.keyword.user.UserKeyword;
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
	 * @param currentUserId 현재 인증된 사용자 ID
	 * @return 추천 영상 목록
	 */
	public YoutubeRecommendResponse getRecommendations(Optional<Long> currentUserId) {
		if(currentUserId.isEmpty()) {
			return getCommonRecommendationsOnly();
		}

		User user = userRepository.findById(currentUserId.get())
			.orElseThrow(() -> new WhiplashException(ErrorStatus.USER_NOT_FOUND));

		List<UserKeyword> userKeywords = userKeywordRepository.findAllByUserOrderByPriority(user);

		if (userKeywords.isEmpty()) {
			log.info("User {} has no keywords. Returning common recommendations only.", user.getId());
			return getCommonRecommendationsOnly();
		} else {
			Set<YoutubeVideo> allRecommendedVideos = getKeywordBasedRecommendationsAndProduceKeywordRecommendTaskIfNeeded(userKeywords);
			int keywordBasedCount = allRecommendedVideos.size();
			log.info("Found {} keyword-based recommendations for user {}", keywordBasedCount, user.getId());

			if (keywordBasedCount == 0) {
				return YoutubeRecommendResponse.empty();
			}

			List<YoutubeVideo> commonVideos = getCommonRecommendations(keywordBasedCount);

			List<YoutubeVideo> allVideos = sumCommonVideosAndRecommendedVideos(allRecommendedVideos, commonVideos);

			List<YoutubeVideoDTO> videoDTOs = allVideos.stream()
				.map(YoutubeVideoDTO::from)
				.collect(Collectors.toList());

			log.info("Returning {} total videos ({} keyword-based, {} common) for user {}",
				videoDTOs.size(), keywordBasedCount, commonVideos.size(), user.getId());

			return YoutubeRecommendResponse.of(videoDTOs, keywordBasedCount, commonVideos.size());
		}
	}

	private static List<YoutubeVideo> sumCommonVideosAndRecommendedVideos(Set<YoutubeVideo> allRecommendedVideos,
		List<YoutubeVideo> commonVideos) {
		List<YoutubeVideo> allVideos = new ArrayList<>();

		allVideos.addAll(allRecommendedVideos);
		allVideos.addAll(commonVideos);
		return allVideos;
	}

	private List<YoutubeVideo> getCommonRecommendations(int keywordBasedCount) {
		List<YoutubeVideo> commonVideos = new ArrayList<>();
		int shortage = MINIMUM_RECOMMEND_COUNT - keywordBasedCount;
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

		return commonVideos;
	}

	private Set<YoutubeVideo> getKeywordBasedRecommendationsAndProduceKeywordRecommendTaskIfNeeded(List<UserKeyword> userKeywords) {
		Set<YoutubeVideo> allRecommendedVideos = new HashSet<>();

		for (UserKeyword userKeyword : userKeywords) {
			List<YoutubeVideo> videosForKeyword = getRecommendedVideosBasedOn(userKeyword);
			addVideosOrProduceKeywordRecommendTask(userKeyword, videosForKeyword, allRecommendedVideos);
		}
		return allRecommendedVideos;
	}

	private void addVideosOrProduceKeywordRecommendTask(UserKeyword userKeyword, List<YoutubeVideo> videosForKeyword,
		Set<YoutubeVideo> allRecommendedVideos) {
		if (videosForKeyword.isEmpty()) {
			// 비동기로 Redis Streams에 키워드 ID 및 이름 발행
			String keywordName = userKeyword.getKeyword().getName();
			youtubeRecommendTaskProducer.produceKeywordRecommendTask(userKeyword.getKeyword().getId(), keywordName);
		}
		allRecommendedVideos.addAll(videosForKeyword);
	}

	private List<YoutubeVideo> getRecommendedVideosBasedOn(UserKeyword userKeyword) {
		List<YoutubeVideo> keywordBasedVideos = new ArrayList<>();
		Set<String> uniqueVideoIds = new HashSet<>();

		Long keywordId = userKeyword.getKeyword().getId();

		// Redis에서 해당 키워드의 추천 영상 조회
		List<YoutubeVideo> videosForKeyword = youtubeRecommendRedisRepository.findByKeywordId(keywordId);

		// 중복 제거하면서 추가
		for (YoutubeVideo video : videosForKeyword) {
			if (!uniqueVideoIds.contains(video.getVideoId())) {
				keywordBasedVideos.add(video);
				uniqueVideoIds.add(video.getVideoId());
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

}
