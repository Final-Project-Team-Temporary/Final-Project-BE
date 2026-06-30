package com.example.whiplash;

import com.example.whiplash.kis.service.KisTokenService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(initializers = TestEnvInitializer.class)
@ActiveProfiles("dev")
@Transactional
@SpringBootTest
public abstract class IntegrationTestSupport {

    // 테스트 시 KIS 외부 API 호출을 막기 위해 mock 처리
    @MockitoBean
    protected KisTokenService kisTokenService;
}
