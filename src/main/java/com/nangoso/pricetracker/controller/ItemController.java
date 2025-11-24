package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.repository.ItemPriceRepository;
import com.nangoso.pricetracker.repository.ItemRepository;
import com.nangoso.pricetracker.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemPriceRepository itemPriceRepository;
    private final ItemService itemService;

    /**
     * 기존 가격 데이터 검증 및 이상치 업데이트 (관리자용)
     */
    @PostMapping("/validate-prices")
    public ResponseEntity<String> validateExistingPrices() {
        itemService.validateAndUpdateExistingPrices();
        return ResponseEntity.ok("Price validation completed successfully");
    }
}
