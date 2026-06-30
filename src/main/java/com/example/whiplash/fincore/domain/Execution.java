package com.example.whiplash.fincore.domain;

import com.example.whiplash.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fin_execution")
public class Execution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity; // 체결 수량

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price; // 체결 가격

    @Column(nullable = false)
    private LocalDateTime executedAt; // 체결 시각
}
