package com.example.whiplash;

import com.example.whiplash.article.summary.web.controller.ArticleSummarizationController;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// @EnableJpaAuditing이 메인 클래스에 있어 @WebMvcTest에서 JPA 메타모델을 찾지 못하는 문제를 mock으로 해결
@ContextConfiguration(initializers = TestEnvInitializer.class)
@WebMvcTest(controllers = {
        ArticleSummarizationController.class
})
public abstract class MvcTestSupport {
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    protected JpaMetamodelMappingContext jpaMetamodelMappingContext;
}
