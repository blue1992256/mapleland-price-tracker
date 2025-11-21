package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * 오늘 가격 DTO
 * 아이템의 오늘 가격 통계 정보
 */
@Data
@AllArgsConstructor
public class TodayPriceDto {
    private LocalDate date;
    private Long avgPrice;
    private Long minPrice;
    private Long maxPrice;
    private Long medianPrice;
    private Integer count;
    private Double changeRate;
}
