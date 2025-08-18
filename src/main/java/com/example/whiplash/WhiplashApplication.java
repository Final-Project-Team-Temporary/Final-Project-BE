package com.example.whiplash;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class WhiplashApplication {

    public static void main(String[] args) {
        // Spring 애플리케이션 시작 전에 .env 파일 로드
        loadEnvFile();
        SpringApplication.run(WhiplashApplication.class, args);
    }

    private static void loadEnvFile() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .filename(".env")
                    .ignoreIfMissing()
                    .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            System.out.println("✅ .env 파일 로드 완료 (총 " + dotenv.entries().size() + "개 변수)");
        } catch (Exception e) {
            System.out.println("⚠️ .env 파일 로드 실패: " + e.getMessage());
        }
    }
}