package com.example.whiplash.global.config;

import java.util.List;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.whiplash.user.web.dto.request.LoginRequestDTO;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(securityRequirement())
            .components(components());

        addLoginEndpoint(openAPI);

        return openAPI;
    }

    private static void addLoginEndpoint(OpenAPI openAPI) {
        // ✅ /api/login 엔드포인트 수동 추가
        // LoginRequest 스키마 정의
        Schema<?> loginSchema = new Schema<>()
            .type("object")
            .addProperty("email", new Schema<>()
                .type("string")
                .format("email")
                .description("사용자 이메일")
                .example("user@example.com"))
            .addProperty("password", new Schema<>()
                .type("string")
                .format("password")
                .description("사용자 비밀번호")
                .example("password123"))
            .required(List.of("email", "password"));

        openAPI.path("/api/login", new PathItem()
            .post(new Operation()
                .summary("로그인")
                .description("이메일과 비밀번호로 로그인합니다. (Spring Security Filter가 실제 처리)")
                .requestBody(new RequestBody()
                    .content(new Content().addMediaType("application/json",
                        new MediaType().schema(loginSchema))))
                .responses(new io.swagger.v3.oas.models.responses.ApiResponses()
                    .addApiResponse("200", new ApiResponse().description("성공"))
                    .addApiResponse("401", new ApiResponse().description("인증 실패"))
                    .addApiResponse("400", new ApiResponse().description("잘못된 요청")))));
    }

    private Info apiInfo() {
        return new Info()
                .title("Whiplash API")
                .description("Whiplash 백엔드 API 문서")
                .version("1.0.0");
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)");
    }
}
