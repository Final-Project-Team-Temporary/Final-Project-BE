package com.example.whiplash;

import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;

public class POJOTestSupport {
    @BeforeAll
    public static void setup() throws IOException {
        TestEnvInitializer testEnvInitializer = new TestEnvInitializer();
        testEnvInitializer.loadEnv();
    }
}
