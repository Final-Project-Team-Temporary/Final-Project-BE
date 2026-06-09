# 성능 테스트 디렉토리

## 구조
```
performance-test/
├── README.md                          # 이 파일
├── SEARCH_API_LOAD_TEST_GUIDE.md     # 용어 검색 API 한계 테스트 상세 가이드
├── test_keywords.csv                  # JMeter용 검색 키워드 데이터
├── jmeter/                            # JMeter 테스트 계획 파일들
│   ├── baseline_test.jmx              # 베이스라인 측정
│   ├── rampup_test.jmx                # 점진적 부하 증가
│   ├── endurance_test.jmx             # 지속 부하 테스트
│   ├── spike_test.jmx                 # 스파이크 테스트
│   └── stress_test.jmx                # 스트레스 테스트
└── results/                           # 테스트 결과 저장 디렉토리
    ├── .gitkeep
    └── [날짜별 결과 파일들]

```

## 빠른 시작

### 1. 테스트 데이터 생성
```bash
cd /Users/seoungbeom/Desktop/Final-Project-BE/scripts
python3 generate_test_data.py --users 100 --terms-per-user 1000
```

### 2. 데이터베이스에 삽입
```bash
mysql -u [username] -p [database] < test_data_insert.sql
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 4. JMeter 테스트 실행
```bash
cd performance-test

# GUI 모드 (테스트 계획 작성/수정)
jmeter

# CLI 모드 (실제 테스트 실행)
jmeter -n -t jmeter/baseline_test.jmx -l results/baseline.jtl -e -o results/baseline_report
```

## 테스트 시나리오별 사용법

### 베이스라인 테스트
단일 사용자 기준 최적 응답 시간 측정
```bash
jmeter -n -t jmeter/baseline_test.jmx -l results/baseline_$(date +%Y%m%d_%H%M%S).jtl
```

### 점진적 부하 테스트
사용자 수를 점진적으로 증가시키며 한계점 파악
```bash
jmeter -n -t jmeter/rampup_test.jmx \
  -Jusers=500 \
  -Jrampup=300 \
  -l results/rampup_$(date +%Y%m%d_%H%M%S).jtl
```

### 스트레스 테스트
시스템 최대 처리 능력 파악
```bash
jmeter -n -t jmeter/stress_test.jmx -l results/stress_$(date +%Y%m%d_%H%M%S).jtl
```

## 결과 분석

### HTML 리포트 생성
```bash
jmeter -g results/your_test.jtl -o results/your_test_report
open results/your_test_report/index.html
```

### 주요 메트릭 확인
- **Average**: 평균 응답 시간
- **Median**: 중앙값 응답 시간
- **90% Line**: 90 퍼센타일 응답 시간
- **95% Line**: 95 퍼센타일 응답 시간
- **99% Line**: 99 퍼센타일 응답 시간
- **Throughput**: 초당 처리량 (TPS)
- **Error %**: 에러 비율

## 모니터링 도구

### JVM 모니터링
```bash
# VisualVM
jvisualvm

# JProfiler (유료)
jprofiler
```

### 데이터베이스 모니터링
```sql
-- 실시간 프로세스
SHOW FULL PROCESSLIST;

-- 느린 쿼리
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 10;
```

### 시스템 리소스
```bash
# CPU, 메모리
top

# 디스크 I/O
iostat -x 1

# 네트워크
netstat -an | grep 8080
```

## 성능 목표

### 최소 요구사항
- 평균 응답 시간: < 500ms
- P95 응답 시간: < 1s
- P99 응답 시간: < 2s
- 에러율: < 1%
- TPS: > 100

### 목표 성능
- 평균 응답 시간: < 300ms
- P95 응답 시간: < 500ms
- P99 응답 시간: < 1s
- 에러율: < 0.1%
- TPS: > 300

## 트러블슈팅

### Connection Refused 에러
- 애플리케이션 실행 여부 확인
- 포트 번호 확인 (기본: 8080)

### Out of Memory 에러
- JVM Heap 크기 증가: `-Xmx4g`
- 데이터베이스 커넥션 풀 조정

### 느린 응답 시간
- 데이터베이스 인덱스 확인
- Slow Query Log 분석
- 애플리케이션 프로파일링

## 참고 문서

- [용어 검색 API 한계 테스트 상세 가이드](./SEARCH_API_LOAD_TEST_GUIDE.md)
- [테스트 데이터 생성 가이드](../scripts/README_TEST_DATA.md)
- [JMeter 공식 문서](https://jmeter.apache.org/usermanual/index.html)