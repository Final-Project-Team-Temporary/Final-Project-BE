---
description: 새 도메인 모듈을 프로젝트 컨벤션에 맞춰 스캐폴딩
---

새 도메인 **$ARGUMENTS** 를 `com.example.whiplash.$ARGUMENTS` 패키지로 추가한다.

기존 도메인(예: `quiz`, `term`) 구조를 참고해 **패키지 바이 피처**로 레이어를 구성한다.

1. 먼저 유사한 기존 도메인 하나를 읽어 실제 패턴(어노테이션·생성자 주입·DTO 네이밍)을 확인한다.
2. 다음 골격을 생성한다(필요한 것만):
   - `controller/` — `@RestController` + `@RequestMapping("/api/$ARGUMENTS")` + `@RequiredArgsConstructor` + `@Slf4j` + `@Tag`, 반환은 `ApiResponse<T>`
   - `service/` — `@Service` + `@RequiredArgsConstructor`, 비즈니스 예외는 `WhiplashException(ErrorStatus.XXX)`
   - `repository/` — JPA `JpaRepository` 또는 Mongo `MongoRepository`
   - `entity/`(JPA) 또는 `document/`(Mongo) — `@Getter` + `@Builder` + protected 기본 생성자
   - `dto/request`, `dto/response` — `XxxReqDto` / `XxxResDto`
3. 새 에러 케이스는 `apiPayload/ErrorStatus.java`에 도메인 주석 구역으로 추가한다.
4. `./gradlew compileJava`로 컴파일을 확인한다.
5. 생성한 파일과 엔드포인트, 추가한 `ErrorStatus`를 요약한다.

실제 비즈니스 로직은 빈 골격 + TODO로 두고, 내가 채울 부분을 명확히 표시한다.
