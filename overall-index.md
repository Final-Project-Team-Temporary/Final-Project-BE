# Whiplash BE - 코드 컨벤션 및 프로젝트 구조 가이드

## 📁 프로젝트 아키텍처

### 패키지 구조 (Clean Architecture)
```
com.example.whiplash/
├── article/                    # 기사 관리 도메인 (NEW - 크롤링/요약)
│   ├── web/                   # Presentation Layer
│   │   ├── controller/        # REST API 엔드포인트
│   │   └── dto/              # 데이터 전송 객체
│   ├── application/          # Application Layer
│   │   ├── service/          # Application Service (Use Cases)
│   │   └── command/          # Command/Query Objects
│   ├── domain/               # Domain Layer
│   │   ├── model/            # Domain Entities & Value Objects
│   │   └── service/          # Domain Services
│   └── repository/           # Infrastructure Layer
│       ├── jpa/              # JPA 구현체 (MySQL)
│       └── mongo/            # MongoDB 구현체
├── auth/                     # 인증/인가 도메인
│   ├── web/
│   ├── application/
│   ├── domain/
│   └── repository/
├── user/                     # 사용자 관리 도메인
│   ├── web/
│   ├── application/
│   ├── domain/
│   └── repository/
├── delivery/                 # 기사 배송 도메인
│   ├── web/
│   ├── application/
│   ├── domain/
│   └── repository/
├── portfolio/                # 포트폴리오 관리
├── recommend/                # 추천 시스템
├── simulation/               # 투자 시뮬레이션
├── trade/                    # 거래 분석/조언
├── translate/                # 번역/설명 생성
├── config/                   # 설정 클래스
└── global/                   # 전역 설정/유틸리티
```

## 🏗️ 아키텍처 패턴

### Clean Architecture 계층
1. **Presentation Layer (web/)**: REST API 엔드포인트, DTO
2. **Application Layer (application/)**: Use Cases, Application Services, Command/Query
3. **Domain Layer (domain/)**: Domain Entities, Value Objects, Domain Services
4. **Infrastructure Layer (repository/)**: 데이터 접근, 외부 서비스 연동

### Strategy Pattern 활용
- `RecommendStrategy`: 추천 알고리즘 전략
- `DispatchStrategy`: 배송 전략 (Queue/Scheduled)
- `RebalanceStrategy`: 포트폴리오 리밸런싱
- `SafetyAnalysisStrategy`: 안전성 분석

### Orchestrator Pattern
- `ArticleDeliveryOrchestrator`: 기사 배송 워크플로우 조정

### CQRS (Command Query Responsibility Segregation) Pattern
- **Command**: 데이터 변경 작업 (Create, Update, Delete)
- **Query**: 데이터 조회 작업 (Read)
- **분리된 모델**: 쓰기와 읽기에 최적화된 별도 모델 사용

## 📝 네이밍 컨벤션

### 클래스 네이밍
```java
// Web Layer - Controller: {Domain}{Purpose}Controller
@RestController
public class ArticleSummarizationCommandController {}
public class ArticleSummarizationQueryController {}

// Application Layer - Handler: {Action}Handler (Application Service 역할)
@Component
public class SummarizeArticleCommandHandler {}
public class GetArticleJobStatusQueryHandler {}
public class GetArticleSummaryQueryHandler {}

// Web Layer - Converter: {Domain}{Purpose}Converter
@Component
public class ArticleSummarizationRequestConverter {}
public class ArticleSummarizationResponseConverter {}

// Repository Interface: {Entity}Repository
public interface ArticleRepository {}

// Repository Implementation: {Entity}{DataStore}Repository
@Repository
public class ArticleJpaRepository implements ArticleRepository {}
public class ArticleMongoRepository implements ArticleRepository {}

// API Request DTO: {Domain}{Purpose}Request (record 기본 사용)
public record ArticleSummarizationRequest(
    @NotEmpty(message = "기사 ID 목록은 필수입니다.")
    List<String> articleIds,
    @NotBlank(message = "요청 소스는 필수입니다.")
    String requestSource,
    String priority
) {}

// API Response DTO: {Domain}{Purpose}Response (record 기본 사용)
public record ArticleSummarizationResponse(
    String jobId,
    String status,
    String message,
    LocalDateTime requestTime
) {}

public record ArticleJobStatusResponse(
    String jobId,
    String status,
    Integer progress,
    LocalDateTime lastUpdated
) {}

public record ArticleSummaryResponse(
    String articleId,
    String title,
    String summary,
    LocalDateTime summarizedAt
) {}

// Domain Entity: PascalCase
public class Article {}
public class SummarizedArticle {}

// Value Object: PascalCase
public class ArticleKeyword {}

// Command/Query Object: {Action}Command/{Action}Query (record 기본 사용)
public record SummarizeArticleCommand(
    List<String> articleIds,
    String requestSource,
    String priority,
    String callbackUrl
) {}

public record GetArticleJobStatusQuery(
    String jobId
) {}

public record GetArticleSummaryQuery(
    List<String> articleIds
) {}
```

### 메서드 네이밍
```java
// 서비스 메서드: 동사 + 명사
public ArticleSummarizationResponse processArticleSummarizationRequest()
public void registerProfile()
public List<UserArticleAssignment> getArticleAssignmentsByStatus()

// Repository 메서드: Spring Data JPA 규칙 따름
List<UserArticleAssignment> findByStatusAndCreatedAtBefore()
```

### 변수 네이밍
```java
// camelCase 사용
private final UserService userService;
private String requestSource;
private List<String> articleIds;

// 상수: UPPER_SNAKE_CASE
public static final String DEFAULT_PRIORITY = "NORMAL";
```

## 🗄️ 데이터베이스 컨벤션

### JPA (MySQL) 엔티티
```java
@Entity
@Table(name = "user_article_assignments") // snake_case 테이블명
@Getter  // Lombok 활용
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserArticleAssignment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "summarized_article_id", nullable = false)
    private String summarizedArticleId;
}
```

### MongoDB 문서
```java
@Document(collection = "articles") // 컬렉션명 명시
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {
    @Id @GeneratedValue
    private String id; // MongoDB ObjectId
    
    private String title;
    private LocalDateTime publishedAt;
}
```

### BaseEntity 패턴
```java
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

## 🌐 API 설계 컨벤션

### REST 엔드포인트 구조
```
/api/{domain}/{resource}/{action}

예시:
POST /api/articles/summarization/request
GET  /api/users/profile-setup
```

### API 응답 표준화
```java
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class ApiResponse<T> {
    @JsonProperty("isSuccess")
    private boolean success;
    private String message;
    private String code;
    private T result;
    
    // 정적 팩터리 메서드
    public static <T> ApiResponse<T> onSuccess(T result)
    public static <T> ApiResponse<T> onCreated(T result)
    public static <T> ApiResponse<T> onFailure(ErrorStatus errorStatus)
}
```

### 컨트롤러 패턴 (Clean Architecture)
```java
// Command Controller - 데이터 변경
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/commands")
public class ArticleSummarizationCommandController {
    
    private final ArticleSummarizationCommandService commandService;
    
    @PostMapping("/summarization")
    public ResponseEntity<ApiResponse<ArticleSummarizationResponse>> createSummarizationJob(
        @Valid @RequestBody ArticleSummarizationRequest request
    ) {
        var result = commandService.createSummarizationJob(request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}

// Query Controller - 데이터 조회
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/queries")
public class ArticleSummarizationQueryController {
    
    private final ArticleSummarizationQueryService queryService;
    
    @GetMapping("/job-status/{jobId}")
    public ResponseEntity<ApiResponse<ArticleJobStatusDTO>> getJobStatus(
        @PathVariable String jobId
    ) {
        var result = queryService.getJobStatus(jobId);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}
```

## 📦 Spring Boot 컨벤션

### 애노테이션 사용
```java
// 서비스 계층
@Service
@RequiredArgsConstructor  // final 필드 생성자 자동 생성
@Slf4j               // 로깅
public class XxxService {}

// 컨트롤러 계층  
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/xxx")
public class XxxController {}

// 설정 클래스
@Configuration
@EnableConfigurationProperties(XxxProperties.class)
public class XxxConfig {}
```

### Validation 패턴 (record 사용)
```java
public record ArticleSummarizationRequest(
    @NotEmpty(message = "기사 ID 목록은 필수입니다.")
    @Size(min = 1, max = 100, message = "기사 ID는 1개 이상 100개 이하여야 합니다.")
    List<String> articleIds,
    
    @NotBlank(message = "요청 소스는 필수입니다.")
    String requestSource,
    
    @Pattern(regexp = "HIGH|NORMAL|LOW", message = "우선순위는 HIGH, NORMAL, LOW 중 하나여야 합니다.")
    String priority
) {
    // Compact Constructor - 추가 검증 로직
    public ArticleSummarizationRequest {
        if (priority == null) {
            priority = "NORMAL"; // 기본값 설정
        }
    }
}
```

### 로깅 패턴
```java
@Slf4j
public class XxxService {
    public void processRequest(RequestDto request) {
        log.info("Processing request from source: {}, count: {}", 
                request.getSource(), request.getIds().size());
        
        log.debug("Generated jobId: {} for items: {}", jobId, items);
        
        // 예외 상황 로깅
        log.error("Failed to process request: {}", request, exception);
    }
}
```

## ⚙️ 설정 컨벤션

### 환경변수 기반 설정
```yaml
spring:
  profiles:
    active: ${PROFILES_ACTIVE}
  
  datasource:
    url: ${RDB_URL}
    username: ${RDB_USERNAME}
    
  data:
    mongodb:
      host: ${NOSQL_HOST}
      database: ${NOSQL_DATABASE}
    
    redis:
      host: ${REDIS_HOST:localhost}  # 기본값 설정
      port: ${REDIS_PORT:6379}
```

### JWT 설정 패턴
```yaml
jwt:
  secret: ${JWT_SECRET}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION}
  issuer: ${JWT_ISSUER}
```

## 🔐 보안 컨벤션

### JWT 인증 패턴
```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;
}
```

### 시큐리티 컨텍스트 활용
```java
@PostMapping("/profile-setup")
public ResponseEntity<ApiResponse<?>> profileSetup(@Valid @RequestBody ProfileRegisterDTO dto) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String userEmail = authentication.getName();
    
    userService.registerProfile(dto, userEmail);
    return ResponseEntity.ok(ApiResponse.onCreated(null));
}
```

## 📈 새로운 기사 크롤링/요약 기능 컨벤션

### 패키지 구조 (article 도메인 - Clean Architecture + CQRS)
```
article/
├── web/                          # Presentation Layer
│   ├── controller/              # REST API 엔드포인트
│   │   ├── ArticleSummarizationCommandController  # Command 처리
│   │   └── ArticleSummarizationQueryController    # Query 처리
│   ├── api/
│   │   └── dto/                # API 데이터 전송 객체
│   │       ├── request/        # API 요청 DTO
│   │       │   └── ArticleSummarizationRequest
│   │       └── response/       # API 응답 DTO
│   │           ├── ArticleSummarizationResponse
│   │           ├── ArticleJobStatusResponse
│   │           └── ArticleSummaryResponse
│   └── converter/              # API ↔ Command/Query 변환
│       ├── ArticleSummarizationRequestConverter
│       └── ArticleSummarizationResponseConverter
├── application/                 # Application Layer
│   ├── cqrs/                   # CQRS Command/Query Objects
│   │   ├── command/            # Command Objects
│   │   │   └── SummarizeArticleCommand
│   │   └── query/              # Query Objects
│   │       ├── GetArticleJobStatusQuery
│   │       └── GetArticleSummaryQuery
│   └── handler/                # Command/Query Handlers (Application Services)
│       ├── SummarizeArticleCommandHandler
│       ├── GetArticleJobStatusQueryHandler
│       └── GetArticleSummaryQueryHandler
├── domain/                     # Domain Layer
│   ├── model/                  # Domain Entities & Value Objects
│   │   ├── Article             # Domain Entity
│   │   ├── SummarizedArticle   # Domain Entity
│   │   ├── ArticleKeyword      # Value Object
│   │   └── JobStatus           # Value Object
│   └── service/                # Pure Domain Services
│       └── ArticleValidationService  # 순수 비즈니스 규칙만
└── repository/                 # Infrastructure Layer
    ├── jpa/                   # JPA 구현체 (MySQL)
    │   ├── ArticleJpaRepository
    │   └── UserArticleAssignmentJpaRepository
    └── mongo/                 # MongoDB 구현체
        ├── ArticleMongoRepository
        └── SummarizedArticleMongoRepository
```

### CQRS 패턴 적용
```java
// Command Object - 요청을 나타내는 객체 (record 사용)
public record SummarizeArticleCommand(
    List<String> articleIds,
    String requestSource,
    String priority,
    String callbackUrl
) {
    // Compact Constructor - 비즈니스 규칙 검증
    public SummarizeArticleCommand {
        Objects.requireNonNull(articleIds, "articleIds는 필수입니다");
        Objects.requireNonNull(requestSource, "requestSource는 필수입니다");
        
        if (articleIds.isEmpty()) {
            throw new IllegalArgumentException("articleIds는 비어있을 수 없습니다");
        }
        
        if (priority == null) {
            priority = "NORMAL";
        }
    }
}

// Query Object - 조회를 나타내는 객체 (record 사용)  
public record GetArticleJobStatusQuery(
    String jobId
) {
    public GetArticleJobStatusQuery {
        Objects.requireNonNull(jobId, "jobId는 필수입니다");
    }
}

public record GetArticleSummaryQuery(
    List<String> articleIds
) {
    public GetArticleSummaryQuery {
        Objects.requireNonNull(articleIds, "articleIds는 필수입니다");
        if (articleIds.isEmpty()) {
            throw new IllegalArgumentException("articleIds는 비어있을 수 없습니다");
        }
    }
}

// Command Handler - Command 처리
@Component
public class SummarizeArticleCommandHandler {
    
    private final ArticleSummarizationDomainService domainService;
    
    public ArticleSummarizationResponse handle(SummarizeArticleCommand command) {
        String jobId = UUID.randomUUID().toString();
        
        // 1. Domain Service 호출
        // 2. Redis 큐에 작업 추가
        
        return new ArticleSummarizationResponse(
                jobId,
                "ACCEPTED",
                "요청이 성공적으로 접수되었습니다.",
                LocalDateTime.now()
        );
    }
}

// Query Handler - Query 처리
@Component
public class GetArticleJobStatusQueryHandler {
    
    public ArticleJobStatusResponse handle(GetArticleJobStatusQuery query) {
        // 조회에 최적화된 별도 로직
        return new ArticleJobStatusResponse(
                query.jobId(),
                "IN_PROGRESS",
                75,
                LocalDateTime.now()
        );
    }
}

@Component
public class GetArticleSummaryQueryHandler {
    
    public List<ArticleSummaryResponse> handle(GetArticleSummaryQuery query) {
        // 읽기 전용 조회 로직
        return articleRepository.findSummariesByIds(query.getArticleIds());
    }
}
```

### 컨트롤러와 핸들러 통합 패턴
```java
// Controller - API ↔ Command/Query 변환 + Handler 호출
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/commands")
public class ArticleSummarizationCommandController {
    
    private final SummarizeArticleCommandHandler commandHandler;
    private final ArticleSummarizationRequestConverter requestConverter;
    private final ArticleSummarizationResponseConverter responseConverter;
    
    @PostMapping("/summarization")
    public ResponseEntity<ApiResponse<ArticleSummarizationResponse>> createSummarizationJob(
        @Valid @RequestBody ArticleSummarizationRequest request
    ) {
        // 1. API DTO → Command 변환
        SummarizeArticleCommand command = requestConverter.toCommand(request);
        
        // 2. Handler 직접 호출 (Service 계층 제거)
        var domainResult = commandHandler.handle(command);
        
        // 3. Domain → API DTO 변환
        ArticleSummarizationResponse response = responseConverter.toResponse(domainResult);
        
        return ResponseEntity.ok(ApiResponse.onSuccess(response));
    }
}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/queries")
public class ArticleSummarizationQueryController {
    
    private final GetArticleJobStatusQueryHandler jobStatusHandler;
    
    @GetMapping("/job-status/{jobId}")
    public ResponseEntity<ApiResponse<ArticleJobStatusResponse>> getJobStatus(
        @PathVariable String jobId
    ) {
        GetArticleJobStatusQuery query = new GetArticleJobStatusQuery(jobId);
        var result = jobStatusHandler.handle(query);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }
}

// Converter - API DTO ↔ Command/Query 변환 책임 분리
@Component
public class ArticleSummarizationRequestConverter {
    
    public SummarizeArticleCommand toCommand(ArticleSummarizationRequest request) {
        return new SummarizeArticleCommand(
                request.articleIds(),
                request.requestSource(),
                request.priority(),
                null // callbackUrl은 별도 처리
        );
    }
}
```

## 💡 개발 가이드라인

### 1. Clean Architecture 기반 개발
1. **Presentation Layer (web/)**: API 엔드포인트, DTO, Converter
2. **Application Layer (application/)**: CQRS 객체와 Handler (Application Service)
3. **Domain Layer (domain/)**: 순수 비즈니스 로직과 도메인 모델
4. **Infrastructure Layer (repository/)**: 데이터 접근과 외부 서비스 연동
5. 의존성 방향: web → application → domain ← repository
6. **계층 간 통신**: Controller → Converter → Handler → Domain Service

### 2. 코드 품질
- **record 우선 사용**: DTO, Command/Query 객체는 record로 구현
- **Compact Constructor 활용**: record의 생성자에서 검증 로직 구현  
- Lombok을 활용한 보일러플레이트 코드 최소화 (Entity/Service 등)
- `@RequiredArgsConstructor`로 의존성 주입
- `@Slf4j`로 일관된 로깅
- `@Valid`를 통한 입력값 검증

### 3. 데이터베이스
- JPA 엔티티는 `BaseEntity` 상속으로 audit 필드 자동 관리
- MongoDB 문서는 `@Document` 애노테이션과 collection 명시
- 복합 데이터베이스 환경에서 적절한 저장소 선택

### 4. API 설계
- RESTful 원칙 준수
- 일관된 응답 형식 (`ApiResponse<T>`)
- 적절한 HTTP 상태 코드 사용
- 한국어 메시지로 사용자 친화적 응답

### 5. CQRS 패턴 적용 가이드라인
- **Command/Query 분리**: Handler가 Application Service 역할 수행
- **Converter 활용**: API DTO ↔ Command/Query 변환 책임 분리
- **순수 Domain Service**: 외부 의존성 없는 순수 비즈니스 규칙만 포함
- **계층 간 결합도 최소화**: 중간 Service 계층 제거로 단순화
- **변환 책임 명확화**: Controller에서 변환 로직 분산 방지

### CQRS 네이밍 컨벤션 (최종)
```java
// Controller
ArticleSummarizationCommandController
ArticleSummarizationQueryController

// Handler (Application Service 역할)
SummarizeArticleCommandHandler
GetArticleJobStatusQueryHandler
GetArticleSummaryQueryHandler

// Converter (변환 책임)
ArticleSummarizationRequestConverter
ArticleSummarizationResponseConverter

// API DTO
api/dto/request/ArticleSummarizationRequest
api/dto/response/ArticleSummarizationResponse
api/dto/response/ArticleJobStatusResponse
api/dto/response/ArticleSummaryResponse

// CQRS 객체
cqrs/command/SummarizeArticleCommand
cqrs/query/GetArticleJobStatusQuery
cqrs/query/GetArticleSummaryQuery

// Domain Service (순수 비즈니스 규칙만)
ArticleValidationService       # 기사 유효성 검증
ArticleBusinessRuleService     # 기사 관련 비즈니스 규칙
```

## 📋 record 사용 가이드라인

### DTO에서 record 사용 시 장점
1. **불변성**: 자동으로 immutable 객체 생성
2. **간결성**: equals, hashCode, toString 자동 생성
3. **가독성**: 보일러플레이트 코드 제거
4. **검증**: Compact Constructor로 생성 시점 검증

### record 사용 예외 상황
- JPA Entity: `@Entity`는 기본 생성자 필요로 인해 class 사용
- 복잡한 비즈니스 로직: 메서드가 많이 필요한 경우 class 고려
- 상속이 필요한 경우: record는 상속 불가

---

이 가이드는 기존 코드베이스를 분석하여 추출한 실제 컨벤션을 기반으로 작성되었으며, **record를 기본으로 하는 DTO 패턴**과 함께 새로운 기사 크롤링/요약 기능 개발 시 이 컨벤션을 따라 일관성을 유지해야 합니다.