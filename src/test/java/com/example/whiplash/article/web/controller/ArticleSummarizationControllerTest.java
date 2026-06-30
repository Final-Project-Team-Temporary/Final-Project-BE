package com.example.whiplash.article.web.controller;

import com.example.whiplash.MvcTestSupport;
import com.example.whiplash.article.original.service.ArticleRegisterInfo;
import com.example.whiplash.article.summary.service.ArticleSummarizationService;
import com.example.whiplash.article.summary.web.controller.ArticleSummarizationController;
import com.example.whiplash.article.summary.web.dto.request.ArticleSummarizationRequest;
import com.example.whiplash.article.summary.web.dto.response.ArticleSummarizationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.internal.matchers.Matches;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
class ArticleSummarizationControllerTest extends MvcTestSupport {
    @MockitoBean
    private ArticleSummarizationService articleSummarizationService;
    @Autowired
    private ArticleSummarizationController articleSummarizationController;

    @DisplayName("크롤링이 끝난 기사의 id 리스트를 작업큐에 등록한다.")
    @Test
    public void should_add_article_id_to_queue_requestSummarization_called() throws Exception {
        // given
        List<String> articleIds = List.of("1", "2", "3");
        ArticleSummarizationRequest request = createArticleSummarizationRequest(articleIds);
        given(articleSummarizationService.processArticleSummarizationRequest(any()))
                .willReturn(ArticleSummarizationResponse.create(true, new ArticleRegisterInfo(articleIds, List.of(), List.of("1", "2", "3")), "message"));

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles/summarization/request")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.processedCount").value(3))
        ;
    }

    @DisplayName("기사 ID 목록이 비어있으면 400 에러를 반환한다.")
    @Test
    public void should_return_bad_request_when_articleIds_is_empty() throws Exception {
        // given
        List<String> emptyArticleIds = List.of();
        ArticleSummarizationRequest request = createArticleSummarizationRequest(emptyArticleIds);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/articles/summarization/request")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.articleIds").value(
                        Matchers.anyOf(
                                Matchers.is("기사 ID 목록은 필수입니다."),
                                Matchers.is("기사 ID는 1개 이상 100개 이하여야 합니다.")
                        )
                ))
        ;

        verify(articleSummarizationService, BDDMockito.times(0))
                .processArticleSummarizationRequest(any());
    }

    private static ArticleSummarizationRequest createArticleSummarizationRequest(List<String> articleIds) {
        return ArticleSummarizationRequest.builder()
                .timestamp(LocalDateTime.of(2025, 5, 1, 0, 0, 0))
                .articleIds(articleIds)
                .build();
    }

}