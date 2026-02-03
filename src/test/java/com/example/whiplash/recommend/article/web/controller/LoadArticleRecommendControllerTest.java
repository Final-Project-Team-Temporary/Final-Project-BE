package com.example.whiplash.recommend.article.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.whiplash.MvcTestSupport;
import com.example.whiplash.recommend.article.application.service.LoadArticleRecommendService;
import com.example.whiplash.recommend.article.web.dto.response.ArticleRecommendItemResponse;

@DisplayName("기사 추천 컨트롤러 테스트")
@AutoConfigureMockMvc(addFilters = false)
class LoadArticleRecommendControllerTest extends MvcTestSupport {

	@MockitoBean
	private LoadArticleRecommendService loadArticleRecommendService;

	@Test
	@DisplayName("기사 추천 API가 성공적으로 추천 목록을 반환해야 한다")
	void should_returnRecommendations_when_validRequest() throws Exception {
		// given
		List<ArticleRecommendItemResponse> articles = createMockArticleDTOs(10);
		Page<ArticleRecommendItemResponse> page = new PageImpl<>(
			articles,
			PageRequest.of(0, 10),
			15
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(page);

		// when & then
		mockMvc.perform(get("/api/recommends/articles")
				.param("page", "0")
				.param("size", "10"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(10))
			.andExpect(jsonPath("$.data.currentPage").value(0))
			.andExpect(jsonPath("$.data.size").value(10))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.hasPrevious").value(false))
			.andExpect(jsonPath("$.data.isFirst").value(true))
			.andExpect(jsonPath("$.data.isLast").value(false));
	}

	@Test
	@DisplayName("기사 추천 API가 두 번째 페이지를 올바르게 반환해야 한다")
	void should_returnSecondPage_when_requestingSecondPage() throws Exception {
		// given
		List<ArticleRecommendItemResponse> articles = createMockArticleDTOs(5);
		Page<ArticleRecommendItemResponse> page = new PageImpl<>(
			articles,
			PageRequest.of(1, 10),
			15
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(page);

		// when & then
		mockMvc.perform(get("/api/recommends/articles")
				.param("page", "1")
				.param("size", "10"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andExpect(jsonPath("$.data.currentPage").value(1))
			.andExpect(jsonPath("$.data.totalElements").value(15))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.hasPrevious").value(true))
			.andExpect(jsonPath("$.data.isFirst").value(false))
			.andExpect(jsonPath("$.data.isLast").value(true));
	}

	@Test
	@DisplayName("기사 추천 API가 빈 페이지를 올바르게 반환해야 한다")
	void should_returnEmptyPage_when_noRecommendations() throws Exception {
		// given
		Page<ArticleRecommendItemResponse> emptyPage = new PageImpl<>(
			List.of(),
			PageRequest.of(0, 10),
			0
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(emptyPage);

		// when & then
		mockMvc.perform(get("/api/recommends/articles")
				.param("page", "0")
				.param("size", "10"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content.length()").value(0))
			.andExpect(jsonPath("$.data.totalElements").value(0))
			.andExpect(jsonPath("$.data.totalPages").value(0));
	}

	@Test
	@DisplayName("기사 추천 API가 기본 페이징 파라미터로 동작해야 한다")
	void should_useDefaultPagingParams_when_noParamsProvided() throws Exception {
		// given
		List<ArticleRecommendItemResponse> articles = createMockArticleDTOs(10);
		Page<ArticleRecommendItemResponse> page = new PageImpl<>(
			articles,
			PageRequest.of(0, 10),
			10
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(page);

		// when & then
		mockMvc.perform(get("/api/recommends/articles"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.currentPage").value(0))
			.andExpect(jsonPath("$.data.size").value(10));
	}

	@Test
	@DisplayName("기사 추천 API가 커스텀 페이지 크기를 지원해야 한다")
	void should_supportCustomPageSize_when_sizeParamProvided() throws Exception {
		// given
		List<ArticleRecommendItemResponse> articles = createMockArticleDTOs(5);
		Page<ArticleRecommendItemResponse> page = new PageImpl<>(
			articles,
			PageRequest.of(0, 5),
			10
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(page);

		// when & then
		mockMvc.perform(get("/api/recommends/articles")
				.param("page", "0")
				.param("size", "5"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.content.length()").value(5))
			.andExpect(jsonPath("$.data.size").value(5));
	}

	@Test
	@DisplayName("기사 추천 API가 DTO 필드를 올바르게 반환해야 한다")
	void should_returnCorrectDTOFields_when_validRequest() throws Exception {
		// given
		ArticleRecommendItemResponse article = new ArticleRecommendItemResponse(
			"article-1",
			"테스트 기사 제목",
			"테스트 언론사",
			"https://example.com/article-1",
			LocalDateTime.of(2025, 1, 15, 12, 0)
		);

		Page<ArticleRecommendItemResponse> page = new PageImpl<>(
			List.of(article),
			PageRequest.of(0, 10),
			1
		);

		given(loadArticleRecommendService.getRecommendedArticles(any(Optional.class), any()))
			.willReturn(page);

		// when & then
		mockMvc.perform(get("/api/recommends/articles"))
			.andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isSuccess").value(true))
			.andExpect(jsonPath("$.data.content[0].id").value("article-1"))
			.andExpect(jsonPath("$.data.content[0].title").value("테스트 기사 제목"))
			.andExpect(jsonPath("$.data.content[0].press").value("테스트 언론사"))
			.andExpect(jsonPath("$.data.content[0].url").value("https://example.com/article-1"))
			.andExpect(jsonPath("$.data.content[0].publishedAt").exists());
	}

	// Helper methods
	private List<ArticleRecommendItemResponse> createMockArticleDTOs(int count) {
		List<ArticleRecommendItemResponse> articles = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			articles.add(new ArticleRecommendItemResponse(
				"article-" + i,
				"테스트 기사 제목 " + i,
				"테스트 언론사 " + i,
				"https://example.com/article-" + i,
				LocalDateTime.now().minusHours(i)
			));
		}
		return articles;
	}
}
