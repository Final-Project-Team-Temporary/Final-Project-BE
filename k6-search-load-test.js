import http from 'k6/http';
import { check, group, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 30 },   // 30초에 30명으로 증가
    { duration: '1m', target: 50 },    // 1분에 50명으로 증가
    { duration: '1m', target: 100 },   // 1분에 100명으로 증가 (최대 부하)
  ],
  thresholds: {
    'http_req_failed': ['rate<0.1'],
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // 다양한 키워드로 검색 요청
  let keywords = ['경제', '금융', '투자', '주식', '암호화폐'];
  let randomKeyword = keywords[Math.floor(Math.random() * keywords.length)];
  
  // URL 인코딩 추가
  let encodedKeyword = encodeURIComponent(randomKeyword);
  
  let searchUrl = `${BASE_URL}/api/articles/search?keyword=${encodedKeyword}&page=0&size=20`;
  let response = http.get(searchUrl);
  
  // console.log("status:", response.status);
  
  check(response, {
    '상태코드 200': (r) => r.status === 200,
    '응답시간 < 500ms': (r) => r.timings.duration < 500,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
  });

  sleep(0.5);
}

