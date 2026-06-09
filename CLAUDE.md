# CLAUDE.md — Whiplash 백엔드 하네스 가이드

> 이 파일은 Claude Code(에이전트)가 매 세션 **가장 먼저 읽는 단일 진실 공급원(source of truth)**입니다.
> 프로젝트의 지도, 컨벤션, 절대 규칙, 자주 쓰는 명령을 담습니다.
> 코드 구조가 바뀌면 이 파일도 함께 갱신하세요. (팀 공유 파일 — git 추적됨)

## 1. 프로젝트 개요

- **이름**: Whiplash (`com.example.whiplash`) — 금융 뉴스 기반 학습 서비스 백엔드
- **핵심 도메인**: 기사 요약/추천, 금융 용어 학습, 퀴즈 생성·풀이, 모의투자, 한국투자증권(KIS) 시세 연동
- **스택**: Java 17, Spring Boot 3.4.5, Gradle
- **데이터 저장소**: MySQL(JPA) + MongoDB + Redis(캐시·Streams)
- **인증**: JWT (jjwt) + Spring Security, 카카오 OAuth
- **비동기/배치**: Quartz, Spring `@Async`, Redis Streams, spring-retry
- **외부 연동**: OpenFeign(KIS·AI 서버), WebFlux WebClient, 메일(Gmail SMTP)
- **API 문서**: springdoc-openapi (Swagger UI: `/swagger-ui.html`)

## 2. 빌드 · 실행 · 테스트 명령

| 목적 | 명령 |
|------|------|
| 컴파일 | `./gradlew compileJava` |
| 전체 빌드(테스트 제외, CI와 동일) | `./gradlew clean bootJar --no-daemon` |
| 전체 테스트 | `./gradlew test` |
| 단일 클래스 테스트 | `./gradlew test --tests "com.example.whiplash.quiz.service.QuizServiceTest"` |
| 패키지 단위 테스트 | `./gradlew test --tests "com.example.whiplash.quiz.*"` |
| 로컬 실행 | `./gradlew bootRun` |
| 의존 인프라(MySQL/Redis/Mongo) 기동 | `docker-compose up -d` |

**환경변수**: 루트 `.env`가 반드시 있어야 한다. 앱(`WhiplashApplication`)과 테스트(`TestEnvInitializer`) 모두 `.env`(테스트는 `.env.test` 우선)를 읽어 시스템 프로퍼티로 주입한다. `.env`는 **git에 절대 올리지 않는다**(gitignore 처리됨).

**테스트 전제**: 통합 테스트는 Testcontainers(Redis/Mongo) + Embedded Mongo를 쓰므로 **Docker 데몬이 실행 중**이어야 한다. KIS 외부 호출은 `IntegrationTestSupport`에서 `kisTokenService`를 `@MockitoBean`으로 막아 둔다.

## 3. 아키텍처 — 모듈 지도

도메인별로 패키지를 나누는 **패키지 바이 피처(package-by-feature)** 구조다. 각 도메인 패키지는 보통 `controller` / `service` / `repository` / `entity`(또는 `document`) / `dto`(`request`·`response`) / `client` / `constant` / `config` 하위로 구성된다.

| 패키지 | 책임 |
|--------|------|
| `article` | 기사 원문 적재(`original`)·요약(`summary`) — JPA + MongoDB 혼합 |
| `term` | 금융 용어 사전·학습(`terms.csv` 기반) |
| `quiz` | 용어 기반 퀴즈 생성/모의고사/주간 챌린지, 사전생성 배치 |
| `recommend` | 기사·유튜브 추천 (Redis Streams 파이프라인) |
| `keyword` | 사용자 키워드 관리 |
| `bookmark` | 기사 북마크 |
| `daily` | 일일 학습 통계 |
| `kis` | 한국투자증권 OpenAPI 토큰·시세 클라이언트 |
| `trade` / `simulation` / `portfolio` / `fincore` | 모의투자(주문·체결·보유·계좌 도메인) |
| `auth` / `user` | 인증(JWT·카카오), 사용자 |
| `log` | 행위 로그(퀴즈 풀이 로그 등) |
| `delivery` / `translate` / `converter` / `recommend` | 부가 도메인·유틸 |
| `apiPayload` | **공통 응답·예외 규약** (아래 4장) |
| `global` | 공통 설정(`config`), 배치 잡(`job`), 이벤트(`event`), 비동기 데코레이터, 헬스체크 |
| `config` | 보안(`security`: JWT·UserPrincipal) 등 부트 설정 |

## 4. 코드 컨벤션 (반드시 준수)

이 프로젝트는 일관된 규약이 잘 잡혀 있다. **새 코드는 주변 코드와 동일한 패턴으로 작성한다.**

### 4.1 API 응답
- 컨트롤러는 항상 `ApiResponse<T>`로 감싸 반환한다.
  - 성공: `return ApiResponse.onSuccess(data);` / 생성: `ApiResponse.onCreated(data)`
  - 실패 응답은 직접 만들지 말고 예외를 던진다(아래).
- 컨트롤러는 `@RestController` + `@RequestMapping("/api/...")` + `@RequiredArgsConstructor` + `@Slf4j` + `@Tag`(Swagger).
- 인증 사용자는 `@AuthenticationPrincipal UserPrincipal principal` → `principal.getUserId()`로 받는다.
- 요청 검증은 `@Valid @RequestBody`, 쿼리 검증은 클래스에 `@Validated` + 파라미터 제약(`@Min` 등).

### 4.2 예외 처리
- 비즈니스 예외는 **반드시** `throw new WhiplashException(ErrorStatus.XXX)` 형태로 던진다. `RuntimeException`/`IllegalArgumentException`을 직접 던지지 않는다.
- 새 에러 케이스는 `apiPayload/ErrorStatus.java` enum에 `CODE("코드", "메시지")`를 도메인 주석 구역에 맞춰 추가한다.
- 전역 처리는 `WhiplashExceptionHandler`가 담당한다 — 개별 컨트롤러에서 try/catch로 응답을 만들지 않는다.

### 4.3 일반
- Lombok 사용: 서비스/컨트롤러는 `@RequiredArgsConstructor`로 생성자 주입(필드 주입 금지). 엔티티는 `@Getter` + `@Builder` + 보호된 기본 생성자 패턴.
- 로깅은 `@Slf4j` + `log.info(...)`. 한국어 로그 메시지 OK (기존 스타일과 일치).
- 주석·로그·메시지는 **한국어**가 기본(기존 코드 컨벤션). 식별자(클래스/메서드/변수)는 영어.
- DTO 네이밍: 요청 `XxxReqDto`, 응답 `XxxResDto` (기존 패턴 유지).
- 설정값은 하드코딩하지 말고 `.env` → `@Value`/`application.yml`로 주입한다.

## 5. 절대 규칙 (하지 말 것)

- 🚫 `.env`, `.env.*`, 비밀키, AWS/카카오/JWT 시크릿을 커밋하거나 코드/로그에 노출하지 않는다.
- 🚫 `main`/`develop`에 직접 푸시하지 않는다. 작업은 항상 `feature/#이슈번호-설명` 브랜치에서.
- 🚫 사용자가 요청하지 않은 커밋·푸시·PR 생성을 임의로 하지 않는다.
- 🚫 `ApiResponse`/`WhiplashException` 규약을 우회한 응답·예외 처리를 만들지 않는다.
- 🚫 운영 DB 스키마를 임의 변경하거나 `spring.jpa.hibernate.ddl-auto`를 함부로 바꾸지 않는다.
- ⚠️ 외부 API(KIS·AI 서버) 호출 코드를 추가할 때 테스트에서 실제 호출이 일어나지 않도록 mock 처리한다.

## 6. 작업 워크플로우 (Plan → Work → Verify)

기능 추가/개선 작업은 다음 루프를 따른다. 큰 변경일수록 1번을 충실히 한다.

1. **Plan(계획)**: 관련 도메인 패키지와 컨벤션을 먼저 읽고, 변경 범위·영향 파일·필요한 `ErrorStatus`/DTO/엔드포인트를 정리한다. 모호하면 추측하지 말고 질문한다.
2. **Work(구현)**: 기존 패턴(4장)을 그대로 따라 최소 변경으로 구현한다. 새 도메인이면 `controller/service/repository/dto` 레이어를 갖춰 추가한다.
3. **Verify(검증)**: `./gradlew compileJava`로 컴파일 확인 → 관련 테스트 작성/실행(`./gradlew test --tests ...`). 가능하면 해당 기능의 단위/통합 테스트를 추가한다. 결과(통과/실패)를 있는 그대로 보고한다.
4. **Report**: 변경 파일, 추가 엔드포인트, 새 `ErrorStatus`, 남은 TODO를 요약한다.

자주 쓰는 작업은 슬래시 커맨드로 표준화되어 있다: `/feature`, `/new-domain`, `/review-diff`. ( `.claude/commands/` 참고 )

## 7. 테스트 작성 가이드

베이스 클래스를 상속해 테스트 유형에 맞게 작성한다. 자세한 규약은 `src/test/java/com/example/whiplash/CLAUDE.md` 참고.

- `IntegrationTestSupport` — `@SpringBootTest` + `dev` 프로필 + `@Transactional` 통합 테스트
- `MvcTestSupport` — `@WebMvcTest` 컨트롤러 슬라이스 테스트
- `MongoTestSupport` / `RedisTestSupport` / `MongoRedisTestSupport` — Testcontainers 기반
- `POJOTestSupport` — 스프링 컨텍스트 불필요한 순수 단위 테스트
