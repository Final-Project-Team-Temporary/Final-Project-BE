package com.example.whiplash.fincore.domain;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "fin_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type; // BUY(매수), SELL(매도)

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price; // 지정가

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity; // 주문 수량

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PENDING, PARTIALLY_FILLED, FILLED, CANCELLED

    // 미체결 잔량 (체결될 때마다 감소)
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    public enum OrderType { BUY, SELL }
    public enum OrderStatus { PENDING, PARTIALLY_FILLED, FILLED, CANCELLED }
}
