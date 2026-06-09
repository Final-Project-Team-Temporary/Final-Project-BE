#!/usr/bin/env python3
"""
용어 API 성능 테스트용 대량 데이터 생성 스크립트

사용법:
    python3 generate_test_data.py --users 100 --terms-per-user 1000

생성 파일:
    - test_data_insert.sql: 실제 삽입용 SQL
    - test_data_stats.txt: 데이터 통계
"""

import random
import argparse
from datetime import datetime, timedelta

# 실제 금융 용어 목록 (검색 테스트용)
FINANCIAL_TERMS = [
    # 기본 금융 용어 (50개)
    "금리", "환율", "주식", "채권", "펀드", "ETF", "배당금", "이자율", "신용등급", "증권",
    "선물", "옵션", "스왑", "헤지", "레버리지", "유동성", "변동성", "베타", "알파", "샤프비율",
    "ROE", "ROA", "PER", "PBR", "EPS", "BPS", "시가총액", "유통주식수", "자기자본", "부채비율",
    "유동비율", "당좌비율", "매출액", "영업이익", "당기순이익", "EBITDA", "FCF", "CAPEX", "감가상각", "자본금",
    "증자", "감자", "배당수익률", "배당성향", "주당배당금", "액면가", "호가", "체결", "미체결", "청약",

    # 심화 용어 (50개)
    "파생상품", "콜옵션", "풋옵션", "행사가격", "만기일", "프리미엄", "시간가치", "내재가치", "델타", "감마",
    "세타", "베가", "로", "임플라이드볼래틸리티", "히스토리컬볼래틸리티", "블랙숄즈", "이항모형", "그릭스", "커버드콜", "프로텍티브풋",
    "스트래들", "스트랭글", "버터플라이", "콘도르", "캘린더스프레드", "수직스프레드", "수평스프레드", "대각스프레드", "레이쇼스프레드", "박스스프레드",
    "컨버전", "리버설", "신세틱포지션", "어비트리지", "스프레드거래", "베이시스", "롤오버", "마진콜", "증거금", "위탁증거금",
    "유지증거금", "초기증거금", "추가증거금", "반대매매", "강제청산", "손절매", "익절매", "스톱로스", "리밋오더", "마켓오더",

    # 거시경제 용어 (30개)
    "GDP", "인플레이션", "디플레이션", "스태그플레이션", "경기침체", "경기부양", "양적완화", "긴축정책", "기준금리", "통화정책",
    "재정정책", "무역수지", "경상수지", "자본수지", "외환보유액", "국가신용등급", "소버린리스크", "CDS프리미엄", "국채수익률", "회사채수익률",
    "예대마진", "순이자마진", "BIS자기자본비율", "NPL", "LTV", "DTI", "DSR", "담보인정비율", "신용대출", "주택담보대출",

    # 투자 전략 (20개)
    "가치투자", "성장투자", "모멘텀투자", "역발상투자", "배당투자", "퀀트투자", "알고리즘매매", "고빈도거래", "차익거래", "페어트레이딩",
    "롱숏전략", "마켓뉴트럴", "이벤트드리븐", "디스트레스", "액티비스트", "매크로전략", "CTA전략", "글로벌매크로", "멀티전략", "펀드오브펀드",

    # ESG 및 최신 용어 (20개)
    "ESG", "지속가능경영", "탄소중립", "그린본드", "소셜본드", "지배구조", "이사회", "사외이사", "독립이사", "감사위원회",
    "내부통제", "준법감시", "리스크관리", "컴플라이언스", "공시", "IR", "주주총회", "의결권", "소액주주", "기관투자자",

    # 암호화폐 및 핀테크 (20개)
    "비트코인", "이더리움", "블록체인", "NFT", "DeFi", "DAO", "스마트컨트랙트", "메타버스", "CBDC", "스테이블코인",
    "마이닝", "스테이킹", "에어드롭", "ICO", "IEO", "토큰", "알트코인", "가스비", "웹3", "P2P금융",

    # 부동산 금융 (20개)
    "REITs", "PF대출", "브릿지론", "메자닌", "선순위", "후순위", "NPL", "경매", "공매", "분양권",
    "입주권", "재건축", "재개발", "리모델링", "토지거래허가", "투기과열지구", "조정대상지역", "분양가상한제", "청약가점제", "전매제한",
]

# 검색 패턴별 용어 그룹
SEARCH_PATTERNS = {
    "금": ["금리", "금융", "금전신탁", "금융상품", "금융기관"],
    "주": ["주식", "주가", "주주", "주당순이익", "주택담보대출"],
    "채": ["채권", "채권수익률", "채권금리", "회사채", "국채"],
    "투": ["투자", "투자신탁", "투자자", "투자수익", "투자전략"],
    "수": ["수익", "수익률", "수익성", "수출", "수입"],
}

def generate_terms_data(num_terms=200):
    """Terms 테이블 데이터 생성"""
    terms = []
    used_terms = set()
    term_id = 1

    # 실제 금융 용어 사용
    for term in FINANCIAL_TERMS:
        if term not in used_terms:
            explanation = f"{term}에 대한 설명입니다. 금융 및 투자 관련 중요 개념으로 실무에서 자주 사용됩니다."
            terms.append((term_id, term, explanation))
            used_terms.add(term)
            term_id += 1

    # 검색 패턴별 추가 용어 (검색 테스트용)
    for prefix, term_list in SEARCH_PATTERNS.items():
        for term in term_list:
            if term not in used_terms:
                explanation = f"{term}에 대한 설명입니다."
                terms.append((term_id, term, explanation))
                used_terms.add(term)
                term_id += 1

    return terms

def generate_user_terms_data(num_users, terms_per_user, terms_data):
    """UserTerms 테이블 데이터 생성"""
    user_terms = []
    user_term_id = 1

    term_ids = [t[0] for t in terms_data]

    for user_id in range(1, num_users + 1):
        # 사용자마다 랜덤하게 용어 선택 (중복 허용하지 않음)
        num_terms = min(terms_per_user, len(term_ids))
        selected_terms = random.sample(term_ids, num_terms)

        # 시간 분산 (최근 1년간 저장한 것으로 가정)
        base_date = datetime.now() - timedelta(days=365)

        for idx, term_id in enumerate(selected_terms):
            # 시간 순서대로 저장 (최근 것일수록 나중에)
            created_at = base_date + timedelta(days=idx * (365 / num_terms))
            created_str = created_at.strftime('%Y-%m-%d %H:%M:%S')

            user_terms.append((user_term_id, user_id, term_id, created_str))
            user_term_id += 1

    return user_terms

def generate_sql_file(terms_data, user_terms_data, output_file):
    """SQL 파일 생성"""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("-- 성능 테스트용 데이터 삽입 스크립트\n")
        f.write(f"-- 생성 시각: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n")
        f.write(f"-- Terms: {len(terms_data)}개, UserTerms: {len(user_terms_data)}개\n\n")

        f.write("SET autocommit = 0;\n")
        f.write("SET unique_checks = 0;\n")
        f.write("SET foreign_key_checks = 0;\n\n")

        # Terms 데이터 삽입
        f.write("-- Terms 테이블 데이터\n")
        f.write("INSERT INTO terms (id, term_name, ai_explanation, created_at, updated_at) VALUES\n")

        for idx, (term_id, term_name, explanation) in enumerate(terms_data):
            created_at = (datetime.now() - timedelta(days=len(terms_data) - idx)).strftime('%Y-%m-%d %H:%M:%S')

            comma = "," if idx < len(terms_data) - 1 else ";"
            f.write(f"({term_id}, '{term_name}', '{explanation}', '{created_at}', '{created_at}'){comma}\n")

        f.write("\n")

        # UserTerms 데이터 삽입 (배치로 나누기)
        batch_size = 1000
        f.write("-- UserTerms 테이블 데이터 (배치 단위 삽입)\n")

        for batch_start in range(0, len(user_terms_data), batch_size):
            batch_end = min(batch_start + batch_size, len(user_terms_data))
            batch = user_terms_data[batch_start:batch_end]

            f.write(f"\n-- Batch {batch_start // batch_size + 1}: {batch_start + 1} ~ {batch_end}\n")
            f.write("INSERT INTO user_terms (id, user_id, terms_id, created_at, updated_at) VALUES\n")

            for idx, (user_term_id, user_id, term_id, created_at) in enumerate(batch):
                comma = "," if idx < len(batch) - 1 else ";"
                f.write(f"({user_term_id}, {user_id}, {term_id}, '{created_at}', '{created_at}'){comma}\n")

            if batch_end < len(user_terms_data):
                f.write("COMMIT;\n")

        f.write("\n")
        f.write("SET foreign_key_checks = 1;\n")
        f.write("SET unique_checks = 1;\n")
        f.write("COMMIT;\n")
        f.write("SET autocommit = 1;\n")

def generate_stats_file(terms_data, user_terms_data, num_users, terms_per_user, output_file):
    """통계 파일 생성"""
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("=" * 60 + "\n")
        f.write("테스트 데이터 생성 통계\n")
        f.write("=" * 60 + "\n\n")

        f.write(f"생성 시각: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        f.write("[데이터 규모]\n")
        f.write(f"- 사용자 수: {num_users:,}명\n")
        f.write(f"- 사용자당 용어 수: {terms_per_user:,}개\n")
        f.write(f"- 총 용어 종류: {len(terms_data):,}개\n")
        f.write(f"- 총 UserTerms 레코드: {len(user_terms_data):,}개\n\n")

        f.write("[예상 데이터 크기]\n")
        terms_size = len(terms_data) * 150  # 평균 150바이트
        user_terms_size = len(user_terms_data) * 50  # 평균 50바이트
        total_size = terms_size + user_terms_size
        f.write(f"- Terms 테이블: {terms_size / 1024 / 1024:.2f} MB\n")
        f.write(f"- UserTerms 테이블: {user_terms_size / 1024 / 1024:.2f} MB\n")
        f.write(f"- 총 예상 크기: {total_size / 1024 / 1024:.2f} MB\n\n")

        f.write("[검색 테스트 키워드]\n")
        for prefix in SEARCH_PATTERNS.keys():
            matching = [t[1] for t in terms_data if prefix in t[1]]
            f.write(f"- '{prefix}' 포함 용어: {len(matching)}개\n")
        f.write("\n")

        f.write("[사용 방법]\n")
        f.write("1. MySQL 접속:\n")
        f.write("   mysql -u [username] -p [database_name]\n\n")
        f.write("2. SQL 파일 실행:\n")
        f.write("   source test_data_insert.sql;\n\n")
        f.write("3. 또는 명령줄에서 직접:\n")
        f.write("   mysql -u [username] -p [database_name] < test_data_insert.sql\n\n")

        f.write("[성능 테스트 시나리오]\n")
        f.write("1. 목록 조회 (페이징):\n")
        f.write("   GET /api/users/terms?page=0&size=20\n\n")
        f.write("2. 용어 검색 (LIKE 쿼리):\n")
        f.write("   GET /api/users/terms/search?keyword=금&page=0&size=20\n\n")
        f.write("3. 자동완성:\n")
        f.write("   GET /api/users/terms/search/suggestions?keyword=금\n\n")

def main():
    parser = argparse.ArgumentParser(description='용어 API 성능 테스트용 데이터 생성')
    parser.add_argument('--users', type=int, default=100, help='사용자 수 (기본: 100)')
    parser.add_argument('--terms-per-user', type=int, default=1000, help='사용자당 용어 수 (기본: 1000)')
    parser.add_argument('--output', type=str, default='test_data_insert.sql', help='출력 SQL 파일명')

    args = parser.parse_args()

    print(f"테스트 데이터 생성 중...")
    print(f"- 사용자 수: {args.users:,}명")
    print(f"- 사용자당 용어 수: {args.terms_per_user:,}개")
    print(f"- 총 예상 레코드 수: {args.users * args.terms_per_user:,}개\n")

    # Terms 데이터 생성
    print("1. Terms 데이터 생성 중...")
    terms_data = generate_terms_data()
    print(f"   ✓ {len(terms_data):,}개 용어 생성 완료")

    # UserTerms 데이터 생성
    print("2. UserTerms 데이터 생성 중...")
    user_terms_data = generate_user_terms_data(args.users, args.terms_per_user, terms_data)
    print(f"   ✓ {len(user_terms_data):,}개 레코드 생성 완료")

    # SQL 파일 생성
    print(f"3. SQL 파일 생성 중: {args.output}")
    generate_sql_file(terms_data, user_terms_data, args.output)
    print(f"   ✓ SQL 파일 생성 완료")

    # 통계 파일 생성
    stats_file = args.output.replace('.sql', '_stats.txt')
    print(f"4. 통계 파일 생성 중: {stats_file}")
    generate_stats_file(terms_data, user_terms_data, args.users, args.terms_per_user, stats_file)
    print(f"   ✓ 통계 파일 생성 완료")

    print("\n" + "=" * 60)
    print("생성 완료!")
    print("=" * 60)
    print(f"\n생성된 파일:")
    print(f"  1. {args.output} - SQL 삽입 스크립트")
    print(f"  2. {stats_file} - 데이터 통계 및 사용 가이드")
    print(f"\n다음 명령으로 데이터베이스에 삽입:")
    print(f"  mysql -u [username] -p [database_name] < {args.output}")

if __name__ == "__main__":
    main()