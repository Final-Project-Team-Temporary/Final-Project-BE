package com.example.whiplash;

import com.example.whiplash.article.web.controller.ArticleSummarizationController;
import com.example.whiplash.config.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
}
