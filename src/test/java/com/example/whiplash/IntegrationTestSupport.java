package com.example.whiplash;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@ContextConfiguration(initializers = TestEnvInitializer.class)
@Transactional
@SpringBootTest
public class IntegrationTestSupport {
}
