/*
package com.example.whiplash.article.ai.service;

import com.example.whiplash.article.ai.AiServerInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableAutoConfiguration(exclude = {MailSenderAutoConfiguration.class})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ai.stub.success-rate=0.8",
        "ai.stub.min-delay-seconds=1",
        "ai.stub.max-delay-seconds=2",
        "GMAIL_HOST=localhost",
        "GMAIL_PORT=587",
        "GMAIL_USERNAME=test@test.com",
        "GMAIL_PASSWORD=test"
})
@DisplayName("SummaryStubService 프로파일별 빈 주입 테스트")
class SummaryStubServiceProfileTest {

    @Autowired
    private AiServerInterface aiServerInterface;

    @Test
    @DisplayName("test 프로파일에서 SummaryStubService가 정상적으로 주입되어야 한다")
    void should_injectSummaryStubService_when_testProfile() {
        // then
        assertThat(aiServerInterface)
                .isNotNull()
                .isInstanceOf(SummaryStubService.class);
    }
}*/
