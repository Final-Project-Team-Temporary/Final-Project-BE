# 테스트 컨벤션 (경로 스코프 규칙)

> 이 디렉터리 하위 테스트 코드를 작성·수정할 때 적용된다. 루트 `CLAUDE.md`의 규약 위에 추가된다.

## 베이스 클래스 선택

테스트는 직접 어노테이션을 붙이지 말고 **반드시 아래 베이스 클래스를 상속**한다.

| 베이스 | 용도 | 특징 |
|--------|------|------|
| `POJOTestSupport` | 스프링 컨텍스트 불필요한 순수 단위 테스트 | 가장 빠름. 협력 객체는 직접 `new` 또는 Mockito |
| `IntegrationTestSupport` | 서비스/리포지토리 통합 테스트 | `@SpringBootTest` + `dev` 프로필 + `@Transactional`(자동 롤백). KIS는 mock |
| `MvcTestSupport` | 컨트롤러 슬라이스 | `@WebMvcTest` + `MockMvc` + `objectMapper`. 새 컨트롤러는 `controllers={}`에 추가 |
| `RedisTestSupport` | Redis 필요 | Testcontainers `redis:7.2`, 각 테스트 후 flushAll |
| `MongoTestSupport` / `MongoRedisTestSupport` | Mongo(±Redis) 필요 | Testcontainers |

## 작성 규칙 (기존 스타일 준수)

- 프레임워크: JUnit 5 + **AssertJ**(`Assertions.assertThat(...)`). Mockito로 외부 의존성 mock.
- `@DisplayName`에 **한국어**로 "~할 수 있다 / ~하면 ~한다" 형태의 행위 설명을 단다.
- 메서드 본문은 `//given`, `//when`, `//then` 3단 구조로 나눈다.
- 테스트 클래스명은 `대상클래스명 + Test`, 패키지는 대상과 동일 경로.
- 환경값이 필요하면 `System.getProperty("JWT_SECRET")`처럼 `.env(.test)`로 주입된 값을 읽는다.

## 전제

- 통합/Testcontainers 테스트는 **Docker 데몬 실행 필수**.
- 외부 API(KIS·AI 서버) 실제 호출이 일어나지 않도록 mock 한다.
- 통합 테스트는 `@Transactional`로 롤백되므로 테스트 간 데이터 격리를 신뢰해도 된다(단, 별도 트랜잭션/비동기 흐름은 예외).
