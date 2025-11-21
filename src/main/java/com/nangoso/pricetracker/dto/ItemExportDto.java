package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * JSON 내보내기용 DTO
 * 아이템 정보를 JSON 파일로 저장할 때 사용
 */
@Data
@AllArgsConstructor
public class ItemExportDto {
    private String itemCode;
    private String name;
    private String imageUrl;
}
