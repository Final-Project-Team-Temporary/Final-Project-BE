# 금융 용어 사전 CSV 데이터 가이드

## 현재 상태

### 생성된 용어 수: 596개

### 카테고리 구성:
1. 거시경제: 40개
2. 주식투자: 81개
3. 회계/재무: 91개
4. 핀테크/IT금융: 77개
5. 파생상품: 68개
6. 부동산금융: 68개
7. ESG/지속가능경영: 96개
8. 추가 용어: 75개

## 10,000개 이상으로 확장하는 방법

### 방법 1: 추가 카테고리 확장 (권장)

다음 카테고리의 용어를 추가하여 10,000개 이상 달성 가능:

#### 추가 필요 카테고리 (예상 용어 수):
1. **보험 용어** (500개)
   - 생명보험, 손해보험, 실손보험, 자동차보험, 여행자보험 등

2. **세금 용어** (400개)
   - 소득세, 법인세, 부가가치세, 상속세, 증여세, 양도소득세 등

3. **대출 용어** (600개)
   - 신용대출, 담보대출, 마이너스통장, 중도상환, 대환대출 등

4. **카드/결제 용어** (300개)
   - 신용카드, 체크카드, 할부, 리볼빙, 포인트, 마일리지 등

5. **예금/적금 용어** (300개)
   - 정기예금, 정기적금, 자유적금, 복리, 단리, 만기, 중도해지 등

6. **연금 용어** (400개)
   - 국민연금, 퇴직연금, 개인연금, IRP, DC형, DB형 등

7. **펀드 용어** (500개)
   - 주식형펀드, 채권형펀드, 혼합형펀드, MMF, ETF, 인덱스펀드 등

8. **금융기관 용어** (300개)
   - 시중은행, 지방은행, 인터넷전문은행, 증권사, 보험사 등

9. **외환 용어** (400개)
   - 환전, 송금, 환차익, 환차손, 외화예금, 외화적금 등

10. **상품금융 용어** (300개)
    - 금, 은, 원유, 구리, 곡물 선물 등

11. **산업별 금융 용어** (1,000개)
    - IT, 바이오, 자동차, 반도체, 화학, 철강 등 각 산업의 금융 용어

12. **금융법규 용어** (500개)
    - 금융소비자보호법, 자본시장법, 은행법, 보험업법 등

13. **리스크관리 용어** (300개)
    - 시장리스크, 신용리스크, 유동성리스크, VaR 등

14. **재무분석 용어** (500개)
    - 듀폰분석, 비율분석, 추세분석, 현금흐름분석 등

15. **투자전략 용어** (600개)
    - 가치투자, 성장투자, 배당투자, 퀀트투자 등의 세부 전략

16. **채권 용어** (400개)
    - 국채, 회사채, 신종자본증권, 채권금리, 듀레이션 등

17. **기업금융 용어** (400개)
    - M&A, IPO, 유상증자, 전환사채, 기업구조조정 등

18. **행동경제학 용어** (200개)
    - 손실회피, 확증편향, 군집행동, 닻내림효과 등

19. **암호화폐 세부 용어** (400개)
    - 알트코인 종류, 거래소 용어, 지갑 종류 등

20. **금융 통계 용어** (300개)
    - 표준편차, 상관계수, 정규분포, 회귀분석 등

**총 추가 가능: 약 8,200개**
**기존 596개 + 추가 8,200개 = 약 8,796개**

### 방법 2: AI를 활용한 자동 생성

OpenAI API나 Claude API를 사용하여 카테고리별로 용어를 자동 생성하는 스크립트:

```python
import openai  # 또는 anthropic

def generate_terms_with_ai(category: str, count: int):
    prompt = f"""
    {category} 분야의 금융 용어 {count}개를 생성해주세요.
    각 용어는 다음 형식으로 제공해주세요:
    - 용어명과 설명을 포함
    - CSV 형식: termName,termDescription
    """
    # API 호출 및 응답 파싱
    pass
```

### 방법 3: 금융 데이터 소스 활용

다음 출처에서 용어를 수집:

1. **금융감독원 금융용어사전**: http://fss.or.kr
2. **한국은행 경제금융용어사전**: http://ecos.bok.or.kr
3. **한국거래소 증권용어해설**: http://www.krx.co.kr
4. **금융투자협회 용어사전**: http://www.kofia.or.kr
5. **Wikipedia 금융 용어**: https://ko.wikipedia.org

### 방법 4: 조합형 용어 생성

기존 용어를 조합하여 파생 용어 생성:

```python
# 예시
base_terms = ["주식", "채권", "펀드"]
modifiers = ["단기", "장기", "해외", "국내", "성장", "가치"]
types = ["형", "투자", "거래", "시장"]

# 조합 생성
for base in base_terms:
    for modifier in modifiers:
        for type in types:
            combined = f"{modifier}{base}{type}"
            # 예: "단기주식형", "장기채권투자", "해외펀드시장" 등
```

## 현재 CSV 사용 방법

### 1. 데이터베이스 직접 삽입
```bash
# MySQL에 직접 INSERT
python3 scripts/generate_test_data.py --users 10 --terms-per-user 50
```

### 2. Spring Boot에서 CSV 읽기
```java
@Service
public class TermImportService {
    public void importFromCsv(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                Terms term = Terms.builder()
                    .termName(row[0])
                    .AiExplanation(row[1])
                    .build();
                termsRepository.save(term);
            }
        }
    }
}
```

### 3. SQL 파일로 변환
```python
# CSV를 SQL INSERT문으로 변환
import csv

with open('financial_terms.csv', 'r', encoding='utf-8-sig') as f:
    reader = csv.DictReader(f)
    with open('insert_terms.sql', 'w', encoding='utf-8') as out:
        out.write("INSERT INTO terms (term_name, ai_explanation) VALUES\n")
        rows = list(reader)
        for i, row in enumerate(rows):
            comma = "," if i < len(rows) - 1 else ";"
            out.write(f"('{row['termName']}', '{row['termDescription']}'){comma}\n")
```

## 다음 단계

1. 필요한 추가 카테고리 선택
2. 각 카테고리별로 용어 수집 또는 생성
3. 기존 스크립트에 추가
4. 최종 CSV 생성 및 검증

## 문의

추가 카테고리가 필요하거나 특정 분야의 용어를 더 추가하고 싶다면 요청해주세요.