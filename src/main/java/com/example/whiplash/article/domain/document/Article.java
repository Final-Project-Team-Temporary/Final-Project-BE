package com.example.whiplash.article.domain.document;

import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "articles")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {
    @Id @GeneratedValue
    private String id;
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    private String url;
    private String press;
    private SummaryStatus summaryStatus;
//    private Category category;

    /**
     * title: "신임 금감원장-은행권 상견례 D-1…5대 은행장 회동 '주목'",
     *   content: '5대 은행장, 신임 금감원장 간담회 하루 앞두고 조찬 회동ELS 과징금·LTV 담합 의혹 등 산적…"현안 논의 아직 일러"\n' +
     *     '\t\n' +
     *     '\t\t\n' +
     *     '\t\n' +
     *     `서울 시내에 설치된 시중은행 ATM 기기 모습. 2025.2.3/뉴스1 ⓒ News1 허경 기자(서울=뉴스1) 정지윤 기자 = 5대 시중은행장이 이찬진 신임 금융감독원장과 첫 상견례를 하루 앞두고 한자리에 모였다. 단순 친목 도모를 위한 목적이라는 입장이지만 새 금감원장과의 만남을 앞둔 시점인 만큼 이들의 행보에 이목이 쏠리고 있다.27일 금융권에 따르면 이환주 KB국민은행장, 정상혁 신한은행장, 이호성 하나은행장, 정진완 우리은행장, 강태영 NH농협은행장 등 5대 은행장은 27일 오전 서울 모처에서 조찬 모임을 열었다.은행장들의 회동 소식에 새 금감원장과의 간담회를 앞두고 행장들이 모여 '대책 회의'에 나선 것 아니냐는 추측이 나왔지만 이들은 친목 차원에서 만난 것일 뿐이라며 말을 아꼈다. 이 금감원장의 인선 발표 전부터 만찬을 계획하고 있었는데, 한국-베트남 정상회담 일정으로 일부 은행장이 참석하지 못하게 돼 이날 조찬으로 순연됐다는 설명이다.이날 모임에 참석한 한 은행장은 "행장끼리 행사에서만 잠깐 볼 뿐 한 번도 저녁 식사를 해본 적 없어 만나는 것"이라며 "금감원장께서 어떤 말씀을 하실지도 모르는 상황에서 대책 회의를 하거나 그런 건 아니다"라고 했다.간담회에서 은행권의 건의 사항을 제시할 가능성에 대해서도 조심스럽다는 반응을 보였다. 은행권은 '홍콩 H지수 주가연계증권(ELS)' 과징금 산정, '부동산담보대출비율(LTV) 담합 의혹' 등 현안으로 당국과 논의해야 할 과제가 산적한 상황이다.다른 은행장은 "처음부터 이야기하기엔 좀 부담스럽다"며 "많은 사람이 모이는 자리라 (현안에 대해) 이야기하기 어려울 것"이라고 했다.다만 이 원장이 금감원장으로서 의견을 경청하겠다는 메시지를 남긴 만큼 은행권과도 소통에 나설 수 있다는 기대도 나오는 상황이다. 앞서 이 원장은 취임 직후 내부 임직원에게 "의견을 경청하고 수렴해 의사결정하겠다. 독단적으로 일하지 않겠다"는 메시지를 남겼다.한 은행장은 "초반에는 하고 싶은 일이나 생각이 있으시겠지만 현장의 이야기를 많이 들으시겠다는 것 아니겠냐"며 "정책이란 게 하다가 잘못되면 피해를 보는 건 소비자이니 충분히 들으시겠다는 것 같다"고 말했다.한편 이 원장은 28일 은행권 CEO와의 간담회를 시작으로 보험, 금융투자회사, 저축은행 등 업권별 상견례에 나설 예정이다. 정부의 정책 기조에 발맞춘 소비자 보호나 서민 금융 확대 등을 우선 강조할 것으로 전망된다.`,
     *   date: '2025-08-27 11:20:54',
     *   url: 'https://n.news.naver.com/mnews/article/421/0008450383',
     *   summary_status: 'pending'
     */
}