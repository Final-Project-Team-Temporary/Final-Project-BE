package com.example.whiplash.article.domain.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SummaryStatus {
    BEFORE_ENQUEUED("redis 작업 큐에 적재 되기 전"),
    ENQUEUED("redis 작업 큐에 적재 됨"),
    COMPLETED("요약 완료"),
    FAILED("요약 실패"),
    CANCELLED("요약 취소"),
    ;
    private final String description;
}
