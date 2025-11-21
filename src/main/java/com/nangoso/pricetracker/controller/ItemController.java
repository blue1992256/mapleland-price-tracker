package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.dto.ItemResponseDto;
import com.nangoso.pricetracker.entity.Item;
import com.nangoso.pricetracker.repository.ItemRepository;
import com.nangoso.pricetracker.repository.ItemPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final ItemPriceRepository itemPriceRepository;

    /**
     * 모든 아이템 목록 조회 (가격 정보 포함)
     */
    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<Item> items = itemRepository.findAll();

        List<ItemResponseDto> itemDtos = items.stream()
                .map(item -> {
                    // 최신 평균 가격 조회
                    Double avgPrice = itemPriceRepository.findLatestAveragePriceByItem(item);
                    Long averagePrice = (avgPrice != null) ? avgPrice.longValue() : null;

                    return new ItemResponseDto(
                            item.getItemCode(),
                            item.getName(),
                            item.getImageUrl(),
                            averagePrice
                    );
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(itemDtos);
    }
}
