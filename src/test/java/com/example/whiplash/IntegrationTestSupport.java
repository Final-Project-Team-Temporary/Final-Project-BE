package com.example.whiplash;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(initializers = TestEnvInitializer.class)
@Transactional
@SpringBootTest
public abstract class IntegrationTestSupport {
}
