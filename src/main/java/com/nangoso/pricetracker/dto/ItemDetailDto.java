package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

/**
 * 아이템 상세 DTO
 * 아이템 상세 페이지에서 사용
 */
@Data
@AllArgsConstructor
public class ItemDetailDto {
    private String itemCode;
    private String name;
    private String imageUrl;
    private LocalDate createdAt;
    private TodayPriceDto todayPrice;
}
