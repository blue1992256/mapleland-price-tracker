package com.nangoso.pricetracker.controller;

import com.nangoso.pricetracker.dto.PopularItemDto;
import com.nangoso.pricetracker.service.ItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
        List<PopularItemDto> popularItems = itemService.getPopularItems(8);

        model.addAttribute("popularItems", popularItems);
        return "index";
    }

    /**
     * 관리자 페이지
     * 가격 비활성화 관리 페이지를 표시합니다.
     */
    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

}
