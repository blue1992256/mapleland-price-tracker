package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * 가격 히스토리 DTO
 * 일별 가격 통계 정보
 */
@Data
@AllArgsConstructor
public class PriceHistoryDto {
    private LocalDate date;
    private Long avgPrice;
    private Long minPrice;
    private Long maxPrice;
    private Long medianPrice;
    private Integer count;
}
