package com.example.whiplash.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * MongoDB Auditing 활성화 설정.
 *
 * <p>@EnableMongoAuditing을 메인 클래스에서 분리하여 @WebMvcTest 컨텍스트에서
 * mongoMappingContext 빈을 찾지 못하는 문제를 방지합니다.</p>
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}