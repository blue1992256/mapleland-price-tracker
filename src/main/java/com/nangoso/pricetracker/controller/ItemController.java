package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.dto.ItemDetailDto;
import com.nangoso.pricetracker.dto.PriceHistoryDto;
import com.nangoso.pricetracker.service.ItemService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * 아이템 상세 페이지
     * 특정 아이템의 가격 정보와 히스토리를 표시합니다.
     */
    @GetMapping("/{itemCode}")
    public String itemDetail(@PathVariable String itemCode, Model model) {
      // 실제 데이터 조회
      ItemDetailDto item = itemService.getItemDetail(itemCode);

      if (item == null) {
        // 아이템을 찾을 수 없는 경우
        model.addAttribute("item", null);
        model.addAttribute("priceHistory", new ArrayList<>());
        return "item-detail";
      }

      // 가격 히스토리 조회 (최근 30일)
      List<PriceHistoryDto> priceHistory = itemService.getPriceHistory(itemCode, 30);

      model.addAttribute("item", item);
      model.addAttribute("priceHistory", priceHistory);
      return "item-detail";
    }
}
