package com.example.whiplash.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class ObjectMapperConfig {
	@Bean
	ObjectMapper objectMapper() {
		// ObjectMapper 커스터마이징
		ObjectMapper objectMapper = new ObjectMapper();

		setLocalDateTimeModule(objectMapper);

		return objectMapper;
	}

	private static void setLocalDateTimeModule(ObjectMapper objectMapper) {
		objectMapper.registerModule(new JavaTimeModule());	// ISO 8601로 직렬화
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);	// LocalDateTime을 timestamp 형식으로 직렬화 안함
	}
}
