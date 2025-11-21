package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.dto.ItemDetailDto;
import com.nangoso.pricetracker.dto.PriceHistoryDto;
import com.nangoso.pricetracker.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 뷰 컨트롤러
 * Thymeleaf 템플릿에 데이터를 전달하여 HTML 페이지를 렌더링합니다.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final ItemService itemService;

    /**
     * 메인 페이지
     * 인기 아이템 목록을 표시합니다.
     */
    @GetMapping("/")
    public String index(Model model) {
        // TODO: 실제 데이터는 Service에서 조회
        // List<ItemDto> popularItems = itemService.getPopularItems(12);

        // 임시 데이터 (개발 단계)
        List<PopularItemDto> popularItems = createMockPopularItems();

        model.addAttribute("popularItems", popularItems);
        return "index";
    }

    /**
     * 아이템 상세 페이지
     * 특정 아이템의 가격 정보와 히스토리를 표시합니다.
     */
    @GetMapping("/items/{itemCode}")
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

    // ===== 임시 Mock 데이터 생성 메서드 (개발 단계용) =====

    /**
     * 임시 인기 아이템 목록 생성
     */
    private List<PopularItemDto> createMockPopularItems() {
        List<PopularItemDto> items = new ArrayList<>();

        items.add(new PopularItemDto(1L, "하이퍼 부스터", null, 15000000L, 5.2));
        items.add(new PopularItemDto(2L, "명예의 훈장", null, 8500000L, -2.3));
        items.add(new PopularItemDto(3L, "드래곤 스케일 메일", null, 25000000L, 0.0));
        items.add(new PopularItemDto(4L, "매지컬 루비", null, 3200000L, 12.5));
        items.add(new PopularItemDto(5L, "블루 스니커즈", null, 1800000L, -5.1));
        items.add(new PopularItemDto(6L, "레드 베릴", null, 950000L, 3.8));

        return items;
    }

    // ===== DTO 클래스 (내부 클래스) =====

    /**
     * 인기 아이템 DTO
     */
    public static class PopularItemDto {
        private Long id;
        private String name;
        private String imageUrl;
        private Long currentAvgPrice;
        private Double priceChange;

        public PopularItemDto(Long id, String name, String imageUrl, Long currentAvgPrice, Double priceChange) {
            this.id = id;
            this.name = name;
            this.imageUrl = imageUrl;
            this.currentAvgPrice = currentAvgPrice;
            this.priceChange = priceChange;
        }

        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getImageUrl() { return imageUrl; }
        public Long getCurrentAvgPrice() { return currentAvgPrice; }
        public Double getPriceChange() { return priceChange; }
    }
}
