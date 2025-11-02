package com.example.whiplash.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new StringToLocalDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    // String -> LocalDateTime 변환 (다중 형식 지원)
    private static class StringToLocalDateTimeConverter implements
            Converter<String, LocalDateTime> {
        private static final DateTimeFormatter FORMATTER_WITH_SPACE =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        private static final DateTimeFormatter FORMATTER_WITH_T =
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            // ISO 8601 형식 (T 포함) 먼저 시도
            if (source.contains("T")) {
                try {
                    return LocalDateTime.parse(source, FORMATTER_WITH_T);
                } catch (Exception e) {
                    // ISO 8601 표준 형식으로도 시도 (밀리초 포함 가능)
                    return LocalDateTime.parse(source);
                }
            }

            // 공백 구분 형식 시도
            return LocalDateTime.parse(source, FORMATTER_WITH_SPACE);
        }
    }
}

