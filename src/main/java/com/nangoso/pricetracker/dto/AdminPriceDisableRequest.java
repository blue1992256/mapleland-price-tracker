package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 관리자 가격 비활성화 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminPriceDisableRequest {

    private String itemCode;      // 아이템 코드
    private Long price;            // 비활성화할 가격
    private LocalDate date;        // 비활성화할 가격의 날짜
    private String adminPassword;  // 관리자 비밀번호
}
