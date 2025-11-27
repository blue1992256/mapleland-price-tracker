package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 API 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {

    private boolean success;  // 성공 여부
    private String message;   // 응답 메시지

    public static AdminResponse success(String message) {
        return new AdminResponse(true, message);
    }

    public static AdminResponse error(String message) {
        return new AdminResponse(false, message);
    }
}
