package com.example.whiplash;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * MongoDB + Redis 두 컨테이너를 함께 사용하는 테스트 베이스 클래스
 */
@Testcontainers
public abstract class MongoRedisTestSupport extends IntegrationTestSupport {

    static final int MONGO_PORT = 27017;
    static final int REDIS_PORT = 6379;

    @Container
    static GenericContainer<?> mongo =
            new GenericContainer<>("mongo:7.0")
                    .withExposedPorts(MONGO_PORT)
                    .withEnv("MONGO_INITDB_DATABASE", "test")
                    .withEnv("MONGO_INITDB_ROOT_USERNAME", "user")
                    .withEnv("MONGO_INITDB_ROOT_PASSWORD", "1111");

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7.2")
                    .withExposedPorts(REDIS_PORT);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", () -> mongo.getHost());
        registry.add("spring.data.mongodb.port", () -> mongo.getMappedPort(MONGO_PORT));
        registry.add("spring.data.mongodb.database", () -> "test");
        registry.add("spring.data.mongodb.username", () -> "user");
        registry.add("spring.data.mongodb.password", () -> "1111");
        registry.add("spring.data.mongodb.authentication-database", () -> "admin");

        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(REDIS_PORT));
    }

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @AfterEach
    void tearDown() {
        mongoTemplate.getDb().drop();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
    }
}