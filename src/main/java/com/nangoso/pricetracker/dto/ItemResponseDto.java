package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 아이템 API 응답 DTO
 * 검색 및 목록 조회 시 사용
 */
@Data
@AllArgsConstructor
public class ItemResponseDto {
    private String itemCode;
    private String name;
    private String imageUrl;
    private Long averagePrice; // 최신 평균 가격
}
