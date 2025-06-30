# Spring Security JWT 인증 필터 구현 리포트

## 1. Spring Security 필터 체인 개념

### 1.1 필터 체인의 역할
Spring Security는 서블릿 필터 체인을 기반으로 동작합니다. 각 HTTP 요청은 여러 보안 필터를 순차적으로 통과하며, 각 필터는 특정 보안 기능을 담당합니다.

### 1.2 OncePerRequestFilter
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter
```
- **목적**: 요청당 한 번만 실행되는 것을 보장
- **필요성**: 포워드, 리디렉션 시 중복 실행 방지
- **핵심 메서드**: `doFilterInternal()` - 실제 필터 로직 구현

### 1.3 필터 체인 흐름
1. 요청이 들어옴
2. JWT 토큰 추출 및 검증
3. 유효한 토큰일 경우 SecurityContext에 인증 정보 설정
4. 다음 필터로 전달 (`filterChain.doFilter()`)

## 2. JWT 인증 필터의 핵심 클래스들

### 2.1 JwtTokenProvider
```java
private final JwtTokenProvider jwtTokenProvider;
```
**역할**:
- `resolveToken(HttpServletRequest)`: 요청에서 토큰 추출
- `validateToken(String)`: 토큰 유효성 검증
- `getUserIdFromToken(String)`: 토큰에서 사용자 ID 추출
- `getAuthoritiesFromToken(String)`: 토큰에서 권한 정보 추출

### 2.2 UsernamePasswordAuthenticationToken
```java
UsernamePasswordAuthenticationToken authenticationToken = 
    new UsernamePasswordAuthenticationToken(principal, null, authorities);
```
**특징**:
- Spring Security의 표준 인증 토큰
- **첫 번째 파라미터**: Principal (인증된 사용자 정보)
- **두 번째 파라미터**: Credentials (보통 null, JWT에서는 토큰 자체가 자격증명)
- **세 번째 파라미터**: 권한 목록

### 2.3 User (UserDetails 구현체)
```java
User principal = new User(userIdFromToken, "", 
    Collections.singletonList(new SimpleGrantedAuthority(authoritiesFromToken)));
```
**구성 요소**:
- **Username**: 사용자 식별자
- **Password**: JWT 방식에서는 빈 문자열 (토큰이 자격증명 역할)
- **Authorities**: 사용자 권한 목록

## 3. SecurityContext와 Authentication 객체

### 3.1 SecurityContextHolder
```java
SecurityContextHolder.getContext().setAuthentication(authenticationToken);
```
**개념**:
- Spring Security의 핵심 저장소
- 현재 인증된 사용자 정보를 스레드 로컬에 저장
- 애플리케이션 전체에서 인증 정보 접근 가능

### 3.2 SecurityContext 저장 전략
- **ThreadLocal**: 기본 전략, 각 스레드별 독립적 저장
- **InheritableThreadLocal**: 자식 스레드로 상속
- **Global**: 전역 저장 (비권장)

### 3.3 Authentication 객체 구조
```java
public interface Authentication extends Principal, Serializable {
    Collection<? extends GrantedAuthority> getAuthorities();
    Object getCredentials();
    Object getDetails();
    Object getPrincipal();
    boolean isAuthenticated();
}
```

## 4. 권한 시스템

### 4.1 GrantedAuthority
```java
Collections.singletonList(new SimpleGrantedAuthority(authoritiesFromToken))
```
**역할**:
- 사용자가 가진 권한을 표현
- `SimpleGrantedAuthority`: 가장 기본적인 구현체
- 권한명은 보통 "ROLE_" 접두사 사용 (예: "ROLE_USER", "ROLE_ADMIN")

### 4.2 권한 기반 접근 제어
- `@PreAuthorize`: 메서드 실행 전 권한 검사
- `@Secured`: 단순 역할 기반 보안
- `@RolesAllowed`: JSR-250 표준

## 5. 필터 등록 및 설정

### 5.1 필터 등록 방법
```java
@Component  // Spring Bean으로 등록
@RequiredArgsConstructor  // 의존성 주입
```

### 5.2 Security Config에서 필터 체인 설정
```java
http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

## 6. 보안 고려사항

### 6.1 토큰 검증 로직
- **반드시 유효한 토큰만 인증 처리**
- null 체크 필수
- 만료, 변조 검증

### 6.2 필터 체인 연속성
- 인증 실패 시에도 `filterChain.doFilter()` 호출 필수
- 다른 인증 방식으로의 fallback 허용

### 6.3 스레드 안전성
- SecurityContext는 스레드별 격리
- 비동기 처리 시 컨텍스트 전파 고려

## 7. 실무 Best Practices

### 7.1 에러 처리
- 토큰 파싱 오류 시 로깅
- 사용자에게는 최소한의 정보만 노출

### 7.2 성능 최적화
- 토큰 캐싱 고려
- 불필요한 토큰 파싱 방지

### 7.3 로깅 및 모니터링
- 인증 성공/실패 로그
- 보안 이벤트 추적

---
*이 리포트는 JwtAuthenticationFilter 구현을 통해 학습한 Spring Security 핵심 개념들을 정리한 문서입니다.*