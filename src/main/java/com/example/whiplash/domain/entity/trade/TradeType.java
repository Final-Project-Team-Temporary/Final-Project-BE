package com.example.whiplash.domain.entity.trade;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TradeType {
    SPOT("현물"), FUTURES("선물");

    private final String mean;
}
