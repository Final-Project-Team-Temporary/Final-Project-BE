package com.example.whiplash;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class MongoTestSupport extends IntegrationTestSupport{
    static final Integer MONGO_PORT_IN_CONTAINER = 27017;

    @Container
    static GenericContainer<?> mongo =
            new GenericContainer<>("mongo:7.0")
                    .withExposedPorts(MONGO_PORT_IN_CONTAINER)
                    .withEnv("MONGO_INITDB_DATABASE", "test")
                    .withEnv("MONGO_INITDB_ROOT_USERNAME", "user")
                    .withEnv("MONGO_INITDB_ROOT_PASSWORD", "1111")
            ;

    @DynamicPropertySource
    static void overrideRedisProps(DynamicPropertyRegistry registry) {

        registry.add("spring.data.mongodb.host", () -> mongo.getHost());
        registry.add("spring.data.mongodb.port", () -> mongo.getMappedPort(MONGO_PORT_IN_CONTAINER));
        registry.add("spring.data.mongodb.database", () -> "test");
        registry.add("spring.data.mongodb.username", () -> "user");
        registry.add("spring.data.mongodb.password", () -> "1111");
        registry.add("spring.data.mongodb.authentication-database", () -> "admin");

    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @AfterEach
    void tearDown() {
        //모든 Collection 초기화
        mongoTemplate.getDb().drop();
    }
}
