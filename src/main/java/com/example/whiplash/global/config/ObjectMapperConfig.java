package com.example.whiplash.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {

	/**
	 * HTTP Request/Response용 ObjectMapper (Primary)
	 */
	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		setLocalDateTimeModule(objectMapper);
		setDeserializationConfig(objectMapper);
		// ⭐ HTTP용은 타입 정보 비활성화 (클라이언트 JSON에 @class 필드 없음)

		return objectMapper;
	}

	/**
	 * Redis 직렬화/역직렬화 전용 ObjectMapper
	 */
	@Bean("redisObjectMapper")
	public ObjectMapper redisObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		setLocalDateTimeModule(objectMapper);
		setDeserializationConfig(objectMapper);
		setPolymorphicTypeValidation(objectMapper);  // ⭐ Redis용은 타입 정보 활성화

		return objectMapper;
	}

	private static void setLocalDateTimeModule(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());	// ISO 8601로 직렬화
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);	// LocalDateTime을 timestamp 형식으로 직렬화 안함
	}

	private static void setDeserializationConfig(ObjectMapper objectMapper) {
		// 알 수 없는 속성이 있어도 무시 (역직렬화 시 유연성 - Redis에서 필요)
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static void setPolymorphicTypeValidation(ObjectMapper objectMapper) {
		// Redis 역직렬화를 위한 타입 정보 활성화
		PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
			.allowIfBaseType(Object.class)
			.allowIfBaseType(java.util.List.class)
			.allowIfBaseType(java.util.ArrayList.class)
			.build();

		objectMapper.activateDefaultTyping(
			ptv,
			ObjectMapper.DefaultTyping.NON_FINAL,
			JsonTypeInfo.As.PROPERTY
		);
	}
}
