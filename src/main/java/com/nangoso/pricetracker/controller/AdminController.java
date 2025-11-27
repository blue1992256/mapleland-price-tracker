package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.dto.AdminPriceDisableRequest;
import com.nangoso.pricetracker.dto.AdminResponse;
import com.nangoso.pricetracker.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 가격 비활성화 API
     */
    @PostMapping("/disable-price")
    public ResponseEntity<AdminResponse> disablePrice(@RequestBody AdminPriceDisableRequest request) {
        log.info("가격 비활성화 요청: itemCode={}, price={}, date={}",
                 request.getItemCode(), request.getPrice(), request.getDate());

        AdminResponse response = adminService.disablePrice(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
