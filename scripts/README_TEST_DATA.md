# 성능 테스트용 데이터 생성 가이드

## 개요
용어 API의 성능 테스트를 위한 대량 데이터 생성 및 삽입 도구입니다.

## 특징
- ✅ 실제 금융 용어 200+ 사용 (현실적인 검색 패턴)
- ✅ 검색 테스트용 패턴별 용어 그룹화
- ✅ 시간 분산된 데이터 (최근 1년간 저장 시뮬레이션)
- ✅ 배치 삽입으로 최적화 (1,000개 단위)
- ✅ 10만건 기준 ~5초 삽입 속도

## 사용 방법

### 1. 데이터 생성

#### 기본 사용 (100명 사용자, 각 1,000개 용어 = 10만 레코드)
```bash
cd /Users/seoungbeom/Desktop/Final-Project-BE/scripts
python3 generate_test_data.py
```

#### 대규모 데이터 (1,000명 사용자, 각 10,000개 용어 = 1,000만 레코드)
```bash
python3 generate_test_data.py --users 1000 --terms-per-user 10000
```

#### 소규모 테스트 (10명 사용자, 각 100개 용어 = 1,000 레코드)
```bash
python3 generate_test_data.py --users 10 --terms-per-user 100
```

#### 커스텀 파일명 지정
```bash
python3 generate_test_data.py --users 500 --terms-per-user 5000 --output my_test_data.sql
```

### 2. 데이터베이스 설정 확인

#### 대량 삽입 전 MySQL 설정 최적화
```sql
-- 현재 설정 확인
SHOW VARIABLES LIKE 'max_allowed_packet';
SHOW VARIABLES LIKE 'innodb_buffer_pool_size';

-- 필요시 설정 변경 (10만건 이상 삽입 시)
SET GLOBAL max_allowed_packet = 1073741824;  -- 1GB
SET GLOBAL innodb_buffer_pool_size = 2147483648;  -- 2GB (재시작 필요)
```

### 3. 데이터 삽입

#### 방법 1: MySQL CLI에서 직접 실행 (추천)
```bash
# 환경변수에서 DB 정보 가져오기
export DB_HOST="your-db-host"
export DB_NAME="your-db-name"
export DB_USER="your-db-user"

# SQL 파일 실행
mysql -h $DB_HOST -u $DB_USER -p $DB_NAME < test_data_insert.sql
```

#### 방법 2: MySQL 접속 후 실행
```bash
mysql -h $DB_HOST -u $DB_USER -p $DB_NAME

# MySQL 프롬프트에서
mysql> source test_data_insert.sql;
mysql> exit;
```

#### 방법 3: Docker 컨테이너 사용 시
```bash
# 컨테이너 내부로 파일 복사
docker cp test_data_insert.sql mysql-container:/tmp/

# 컨테이너 내부에서 실행
docker exec -i mysql-container mysql -u root -p database_name < /tmp/test_data_insert.sql
```

### 4. 삽입 확인

```sql
-- 데이터 확인
SELECT COUNT(*) FROM terms;
SELECT COUNT(*) FROM user_terms;

-- 사용자별 용어 수 확인
SELECT user_id, COUNT(*) as term_count
FROM user_terms
GROUP BY user_id
ORDER BY user_id
LIMIT 10;

-- 검색 테스트
SELECT COUNT(*) FROM user_terms ut
JOIN terms t ON ut.terms_id = t.id
WHERE t.term_name LIKE '%금%';
```

## 성능 테스트 시나리오

### 시나리오 1: 목록 조회 (페이징)
```bash
# JMeter Thread Group 설정
- Number of Threads: 100
- Ramp-Up Period: 10
- Loop Count: 100

# HTTP Request
GET /api/users/terms?page=${__Random(0,50)}&size=20
Authorization: Bearer ${ACCESS_TOKEN}
```

**예상 결과**:
- 10만건 기준: 평균 응답 시간 < 100ms
- 100만건 기준: 평균 응답 시간 < 200ms

### 시나리오 2: 용어 검색 (LIKE 쿼리)
```bash
# 검색 키워드 변수 설정 (CSV Data Set Config)
keywords: 금,주,채,투,수,이,배,수익,주식,금리

# HTTP Request
GET /api/users/terms/search?keyword=${keyword}&page=0&size=20
Authorization: Bearer ${ACCESS_TOKEN}
```

**예상 결과**:
- LIKE '%keyword%' 패턴: 평균 응답 시간 500ms ~ 2s (인덱스 미사용)
- 인덱스 추가 후: 평균 응답 시간 < 200ms

### 시나리오 3: 자동완성
```bash
# 타이핑 시뮬레이션
keywords: ㄱ,금,금리,금리상승

# HTTP Request
GET /api/users/terms/search/suggestions?keyword=${keyword}
Authorization: Bearer ${ACCESS_TOKEN}
```

**예상 결과**:
- 평균 응답 시간 < 100ms (LIKE 'keyword%' 패턴)

## 데이터 규모별 예상 처리 시간

| 사용자 수 | 용어/사용자 | 총 레코드 | 생성 시간 | 삽입 시간 | 파일 크기 |
|-----------|-------------|-----------|-----------|-----------|-----------|
| 10 | 100 | 1,000 | ~1초 | ~0.1초 | ~100KB |
| 100 | 1,000 | 100,000 | ~5초 | ~5초 | ~10MB |
| 500 | 5,000 | 2,500,000 | ~30초 | ~30초 | ~250MB |
| 1,000 | 10,000 | 10,000,000 | ~2분 | ~2분 | ~1GB |

## 삽입 속도 최적화 팁

### 1. 트랜잭션 설정
```sql
SET autocommit = 0;
SET unique_checks = 0;
SET foreign_key_checks = 0;

-- 데이터 삽입

SET foreign_key_checks = 1;
SET unique_checks = 1;
COMMIT;
SET autocommit = 1;
```

### 2. 인덱스 비활성화 (선택)
```sql
-- 인덱스 비활성화
ALTER TABLE user_terms DISABLE KEYS;

-- 데이터 삽입

-- 인덱스 재생성
ALTER TABLE user_terms ENABLE KEYS;
```

### 3. 배치 크기 조정
스크립트 내부의 `batch_size` 변수를 조정하여 최적화:
- SSD: 5,000 ~ 10,000
- HDD: 1,000 ~ 2,000

## 테스트 후 데이터 정리

```sql
-- 테스트 데이터만 삭제 (주의!)
DELETE FROM user_terms WHERE user_id <= 1000;
DELETE FROM terms WHERE id <= 250;

-- 또는 테이블 전체 초기화
TRUNCATE TABLE user_terms;
TRUNCATE TABLE terms;

-- Auto Increment 초기화
ALTER TABLE user_terms AUTO_INCREMENT = 1;
ALTER TABLE terms AUTO_INCREMENT = 1;
```

## 문제 해결

### 에러: "Packet too large"
```sql
SET GLOBAL max_allowed_packet = 1073741824;
```

### 에러: "Lock wait timeout"
```sql
SET GLOBAL innodb_lock_wait_timeout = 120;
```

### 삽입 속도가 느린 경우
1. 인덱스 확인: `SHOW INDEX FROM user_terms;`
2. MySQL 설정 확인: `innodb_buffer_pool_size`, `innodb_log_file_size`
3. 디스크 I/O 확인: `iostat -x 1`

## 참고

- 생성된 SQL 파일은 Git에 커밋하지 마세요 (용량 문제)
- `.gitignore`에 `scripts/*.sql` 추가 권장
- 프로덕션 환경에서는 절대 실행하지 마세요