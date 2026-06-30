# 용어 검색 API 한계 테스트 가이드

## 목표
`GET /api/users/terms/search` API의 성능 한계를 파악하여:
1. 최대 처리 가능한 동시 사용자 수 (Peak TPS)
2. 응답 시간 임계점 (Response Time Threshold)
3. 시스템 병목 지점 (Bottleneck) 식별
4. 성능 개선 전후 비교

---

## 테스트 환경 구성

### 1. 서버 스펙 확인
```bash
# CPU 코어 수
sysctl -n hw.ncpu

# 메모리
sysctl hw.memsize

# 디스크 I/O
iostat -x 1 5
```

### 2. 데이터베이스 준비

#### A. 테스트 데이터 삽입
```bash
# 단계별 데이터 생성 (점진적 부하 테스트용)
cd /Users/seoungbeom/Desktop/Final-Project-BE/scripts

# Level 1: 1만건 (10명 × 1,000개)
python3 generate_test_data.py --users 10 --terms-per-user 1000 --output level1.sql

# Level 2: 10만건 (100명 × 1,000개)
python3 generate_test_data.py --users 100 --terms-per-user 1000 --output level2.sql

# Level 3: 50만건 (500명 × 1,000개)
python3 generate_test_data.py --users 500 --terms-per-user 1000 --output level3.sql

# Level 4: 100만건 (1,000명 × 1,000개)
python3 generate_test_data.py --users 1000 --terms-per-user 1000 --output level4.sql
```

#### B. 데이터베이스 설정 최적화
```sql
-- 성능 테스트용 MySQL 설정
SET GLOBAL max_connections = 500;
SET GLOBAL innodb_buffer_pool_size = 4294967296;  -- 4GB
SET GLOBAL innodb_log_file_size = 1073741824;     -- 1GB
SET GLOBAL query_cache_size = 0;                   -- Query Cache 비활성화 (테스트용)
SET GLOBAL slow_query_log = ON;
SET GLOBAL long_query_time = 0.5;                  -- 0.5초 이상 쿼리 로깅

-- 현재 인덱스 확인
SHOW INDEX FROM user_terms;
SHOW INDEX FROM terms;
```

### 3. 애플리케이션 설정

#### application.yml 확인
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50          # DB 커넥션 풀 크기
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
        jdbc:
          batch_size: 50
```

---

## JMeter 테스트 계획

### Phase 1: 베이스라인 측정 (현재 성능 파악)

#### 테스트 시나리오 1: 단일 사용자 응답 시간
**목적**: 경합 없는 상태에서의 최적 응답 시간 측정

**JMeter 설정**:
```
Thread Group
├─ Number of Threads: 1
├─ Ramp-Up Period: 0
├─ Loop Count: 100
└─ Duration: -

HTTP Request - Search API
├─ Method: GET
├─ Path: /api/users/terms/search
├─ Parameters:
│  ├─ keyword: ${keyword}  # CSV로 변수화
│  ├─ page: 0
│  └─ size: 20
└─ Headers:
   └─ Authorization: Bearer ${token}

CSV Data Set Config - Keywords
├─ Filename: test_keywords.csv
├─ Variable Names: keyword
└─ Recycle: true
```

**test_keywords.csv 내용**:
```csv
keyword
금
주
채
투
수
금리
주식
채권
투자
수익
금융
주가
채권수익률
투자신탁
수익률
ROE
PER
ETF
펀드
배당
```

**성공 기준**:
- 평균 응답 시간 < 300ms
- P95 응답 시간 < 500ms
- P99 응답 시간 < 1s
- 에러율 0%

---

#### 테스트 시나리오 2: 점진적 부하 증가 (Ramp-Up Test)
**목적**: 동시 사용자 증가에 따른 성능 저하 패턴 파악

**JMeter 설정**:
```
Thread Group
├─ Number of Threads: 500
├─ Ramp-Up Period: 300 (5분)
├─ Loop Count: 10
└─ Duration: 600 (10분)

Stepping Thread Group (플러그인 필요)
├─ This group will start: 50 threads
├─ First, wait for: 30 seconds
├─ Then start: 50 threads every 30 seconds
├─ Continue for: 10 iterations
└─ Stop threads: 50 threads every 30 seconds
```

**모니터링 항목**:
- TPS (Transactions Per Second)
- 평균 응답 시간
- 에러율
- CPU 사용률
- 메모리 사용률
- DB 커넥션 풀 상태

**그래프 생성**:
```
Listener
├─ Response Time Graph
├─ Transactions per Second
├─ Active Threads Over Time
└─ Response Codes per Second
```

---

#### 테스트 시나리오 3: 지속 부하 테스트 (Endurance Test)
**목적**: 장시간 지속 부하 시 메모리 누수, 성능 저하 여부 확인

**JMeter 설정**:
```
Thread Group
├─ Number of Threads: 200
├─ Ramp-Up Period: 60
├─ Loop Count: Infinite
└─ Duration: 3600 (1시간)

Constant Throughput Timer
└─ Target Throughput: 100 requests/min
```

**확인 사항**:
- 시간 경과에 따른 응답 시간 증가 여부
- 메모리 사용량 증가 추세
- GC 빈도 및 시간
- DB 커넥션 누수

---

#### 테스트 시나리오 4: 스파이크 테스트 (Spike Test)
**목적**: 급격한 트래픽 증가 시 시스템 안정성 확인

**JMeter 설정**:
```
# 평상시 부하
Thread Group 1
├─ Number of Threads: 50
├─ Ramp-Up: 10
├─ Loop Count: Infinite
└─ Duration: 600

# 스파이크 (2분 간격으로 발생)
Thread Group 2
├─ Number of Threads: 500
├─ Ramp-Up: 5
├─ Loop Count: 10
└─ Scheduler:
   ├─ Delay: 120 (2분 후 시작)
   └─ Duration: 30 (30초간 실행)
```

**관찰 항목**:
- 스파이크 시 에러율
- 복구 시간 (Recovery Time)
- Circuit Breaker 동작 여부

---

#### 테스트 시나리오 5: 스트레스 테스트 (Stress Test)
**목적**: 시스템이 견딜 수 있는 최대 부하 파악

**JMeter 설정**:
```
Ultimate Thread Group (플러그인)
├─ Start Threads Count: 100, Initial Delay: 0, Startup Time: 60, Hold Load: 300, Shutdown Time: 30
├─ Start Threads Count: 200, Initial Delay: 300, Startup Time: 60, Hold Load: 300, Shutdown Time: 30
├─ Start Threads Count: 400, Initial Delay: 600, Startup Time: 60, Hold Load: 300, Shutdown Time: 30
├─ Start Threads Count: 800, Initial Delay: 900, Startup Time: 60, Hold Load: 300, Shutdown Time: 30
└─ Start Threads Count: 1000, Initial Delay: 1200, Startup Time: 60, Hold Load: 300, Shutdown Time: 30
```

**한계 판단 기준**:
- 에러율 > 5%
- 평균 응답 시간 > 3s
- P95 응답 시간 > 5s
- CPU 사용률 > 90% (지속)
- DB 커넥션 풀 고갈

---

## 모니터링 도구 설정

### 1. JVM 모니터링 (VisualVM 또는 JProfiler)

#### VisualVM 사용
```bash
# VisualVM 실행
jvisualvm

# Spring Boot 애플리케이션에 JMX 설정 추가
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar your-application.jar
```

**모니터링 항목**:
- Heap Memory (Eden, Survivor, Old Gen)
- GC 빈도 및 시간
- Thread Count
- CPU Usage

### 2. 데이터베이스 모니터링

```sql
-- 실시간 쿼리 모니터링
SHOW FULL PROCESSLIST;

-- 느린 쿼리 확인
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 20;

-- 인덱스 사용률
EXPLAIN ANALYZE
SELECT ut.* FROM user_terms ut
JOIN terms t ON ut.terms_id = t.id
WHERE ut.user_id = 1
AND LOWER(t.term_name) LIKE LOWER('%금%');

-- 테이블 통계
SHOW TABLE STATUS LIKE 'user_terms';
SHOW TABLE STATUS LIKE 'terms';

-- 커넥션 상태
SHOW STATUS LIKE 'Threads%';
SHOW STATUS LIKE 'Max_used_connections';
```

### 3. 시스템 리소스 모니터링

```bash
# CPU, 메모리, I/O 실시간 모니터링
# 별도 터미널에서 실행
watch -n 1 'top -l 1 | head -n 10'

# 네트워크 통계
netstat -an | grep 8080 | wc -l  # 활성 연결 수

# 디스크 I/O
iostat -x 1
```

---

## 실전 테스트 실행 절차

### Step 1: 환경 준비
```bash
# 1. 서버 재시작 (초기 상태)
./gradlew bootRun

# 2. 모니터링 도구 시작
jvisualvm &

# 3. 데이터베이스 확인
mysql -u [user] -p -e "SELECT COUNT(*) FROM user_terms; SELECT COUNT(*) FROM terms;"
```

### Step 2: 베이스라인 측정
```bash
# JMeter 명령줄 실행
jmeter -n -t baseline_test.jmx -l baseline_results.jtl -e -o baseline_report

# 결과 확인
open baseline_report/index.html
```

### Step 3: 점진적 부하 테스트
```bash
# 각 데이터 레벨별 테스트
for level in 1 2 3 4; do
  echo "Testing Level $level..."
  jmeter -n -t rampup_test.jmx \
    -Jusers=$((100 * level)) \
    -l level${level}_results.jtl \
    -e -o level${level}_report
done
```

### Step 4: 스트레스 테스트
```bash
# 최대 부하 테스트
jmeter -n -t stress_test.jmx -l stress_results.jtl -e -o stress_report

# 동시에 서버 로그 모니터링
tail -f logs/application.log | grep -E "ERROR|WARN|Exception"
```

---

## 결과 분석 및 판단 기준

### 성능 등급 정의

| 등급 | 평균 응답 시간 | P95 응답 시간 | 에러율 | TPS | 판정 |
|------|---------------|--------------|--------|-----|------|
| S | < 100ms | < 200ms | 0% | > 500 | 매우 우수 |
| A | < 300ms | < 500ms | < 0.1% | > 300 | 우수 |
| B | < 500ms | < 1s | < 1% | > 200 | 양호 |
| C | < 1s | < 2s | < 5% | > 100 | 개선 필요 |
| D | < 3s | < 5s | < 10% | > 50 | 심각한 개선 필요 |
| F | > 3s | > 5s | > 10% | < 50 | 사용 불가 |

### 병목 지점 식별

#### 1. DB 병목
**증상**:
- 응답 시간이 데이터 증가에 비례하여 증가
- `SHOW PROCESSLIST`에서 대기 중인 쿼리 다수
- Slow Query Log에 검색 쿼리 빈번히 출현

**해결 방안**:
```sql
-- 인덱스 추가
CREATE INDEX idx_user_terms_user_id ON user_terms(user_id);
CREATE INDEX idx_terms_name ON terms(term_name);

-- Full-Text Index (더 나은 검색 성능)
ALTER TABLE terms ADD FULLTEXT INDEX ft_term_name(term_name);
```

#### 2. 애플리케이션 병목
**증상**:
- CPU 사용률 > 80%
- GC 시간 증가
- Thread Pool 고갈

**해결 방안**:
```yaml
# application.yml
server:
  tomcat:
    threads:
      max: 200
      min-spare: 10
    accept-count: 100
```

#### 3. 네트워크 병목
**증상**:
- 서버는 여유 있으나 클라이언트 응답 느림
- 네트워크 대역폭 포화

**해결 방안**:
- Response Compression 활성화
- Pagination 크기 축소

---

## 개선 전후 비교 시트

### 테스트 결과 기록 템플릿

```markdown
## 테스트 환경
- 날짜: YYYY-MM-DD
- 데이터 규모: XX만건
- 서버 스펙: CPU XX코어, RAM XXG
- 데이터베이스: MySQL X.X

## 개선 전 (Before)
| 동시 사용자 | 평균 응답 시간 | P95 | P99 | TPS | 에러율 |
|------------|---------------|-----|-----|-----|--------|
| 50 | XXXms | XXXms | XXXms | XX | X% |
| 100 | XXXms | XXXms | XXXms | XX | X% |
| 200 | XXXms | XXXms | XXXms | XX | X% |
| 500 | XXXms | XXXms | XXXms | XX | X% |

**병목 지점**:
- [ ] DB 쿼리
- [ ] CPU
- [ ] 메모리
- [ ] 네트워크

## 개선 후 (After)
| 동시 사용자 | 평균 응답 시간 | P95 | P99 | TPS | 에러율 |
|------------|---------------|-----|-----|-----|--------|
| 50 | XXXms | XXXms | XXXms | XX | X% |
| 100 | XXXms | XXXms | XXXms | XX | X% |
| 200 | XXXms | XXXms | XXXms | XX | X% |
| 500 | XXXms | XXXms | XXXms | XX | X% |

**개선 사항**:
- [ ] 인덱스 추가
- [ ] 쿼리 최적화
- [ ] 캐싱 적용
- [ ] 커넥션 풀 조정

**개선율**: XX% 향상
```

---

## JMeter 테스트 파일 생성

실제 실행 가능한 JMX 파일을 제공하겠습니다. (다음 섹션 참조)

---

## 추천 성능 개선 우선순위

현재 용어 검색 API의 성능 개선 우선순위:

### 1순위: 인덱스 최적화
```sql
-- 복합 인덱스 추가
CREATE INDEX idx_user_terms_user_id ON user_terms(user_id);
CREATE INDEX idx_terms_name_lower ON terms((LOWER(term_name)));

-- Full-Text Search (MySQL 5.7+)
ALTER TABLE terms ADD FULLTEXT INDEX ft_term_name(term_name);

-- 검색 쿼리 변경
SELECT * FROM terms
WHERE MATCH(term_name) AGAINST('금*' IN BOOLEAN MODE);
```

**예상 효과**: 응답 시간 60~80% 감소

### 2순위: Elasticsearch 도입
```yaml
# 검색 전용 엔진 사용
spring:
  elasticsearch:
    rest:
      uris: http://localhost:9200
```

**예상 효과**: 응답 시간 80~90% 감소, 확장성 대폭 향상

### 3순위: Redis 캐싱
```java
@Cacheable(value = "termSearch", key = "#userId + ':' + #keyword")
public Page<DictionaryTermListResDto> searchTerms(Long userId, String keyword, Pageable pageable) {
    // ...
}
```

**예상 효과**: 동일 검색 반복 시 99% 응답 시간 감소

---

## 체크리스트

테스트 실행 전 확인 사항:

- [ ] 테스트 데이터 삽입 완료
- [ ] 데이터베이스 설정 최적화
- [ ] 애플리케이션 서버 재시작
- [ ] JMeter 설치 및 플러그인 확인
- [ ] 모니터링 도구 실행
- [ ] 테스트 시나리오 검토
- [ ] 결과 기록 양식 준비

테스트 실행 중:

- [ ] 실시간 모니터링 확인
- [ ] 에러 로그 모니터링
- [ ] 리소스 사용률 기록
- [ ] 이상 징후 발견 시 즉시 중단

테스트 완료 후:

- [ ] JMeter 리포트 생성
- [ ] 병목 지점 분석
- [ ] 개선 방안 도출
- [ ] 결과 문서화
- [ ] 팀 공유

---

## 문의 및 참고

- JMeter 다운로드: https://jmeter.apache.org/download_jmeter.cgi
- JMeter 플러그인: https://jmeter-plugins.org/
- MySQL 성능 튜닝: https://dev.mysql.com/doc/refman/8.0/en/optimization.html