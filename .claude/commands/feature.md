---
description: 기능 추가/개선 작업을 Plan→Work→Verify 루프로 수행
---

다음 요구사항으로 기능 작업을 수행한다: **$ARGUMENTS**

루트 `CLAUDE.md`의 컨벤션과 작업 워크플로우를 준수하며 아래 순서로 진행한다.

1. **Plan**: 관련 도메인 패키지를 먼저 읽고 영향 범위를 파악한다. 변경/추가할 파일, 필요한 엔드포인트, 새 `ErrorStatus`, DTO(`XxxReqDto`/`XxxResDto`)를 목록으로 제시한다. 요구사항이 모호하면 **구현 전에 질문**한다.
2. **Work**: 기존 패턴(ApiResponse·WhiplashException·@RequiredArgsConstructor·패키지 바이 피처)을 그대로 따라 최소 변경으로 구현한다.
3. **Verify**: `./gradlew compileJava`로 컴파일 확인 후, 변경 대상의 테스트를 작성/수정하고 `./gradlew test --tests "..."`로 실행한다. 결과를 있는 그대로 보고한다.
4. **Report**: 변경 파일 목록, 추가 엔드포인트, 새 `ErrorStatus`, 남은 TODO를 요약한다.

커밋·푸시·PR은 내가 명시적으로 요청하기 전까지 하지 않는다.
