package com.nangoso.pricetracker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "item_prices", indexes = {
    @Index(name = "idx_item_date", columnList = "item_id, date"),
    @Index(name = "idx_item_date_status", columnList = "item_id, date, status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false)
    private LocalDate date; // 가격 수집 날짜

    @Column(nullable = false)
    private Long price; // 판매 가격

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PriceStatus status = PriceStatus.ACTIVE; // 가격 데이터 상태

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = PriceStatus.ACTIVE;
        }
    }

    /**
     * 가격 데이터 상태
     */
    public enum PriceStatus {
        ACTIVE,    // 사용중 (정상 데이터)
        INACTIVE   // 미사용 (이상치로 판단된 데이터)
    }
}
