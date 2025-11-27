package com.nangoso.pricetracker.service;

import com.nangoso.pricetracker.dto.AdminPriceDisableRequest;
import com.nangoso.pricetracker.dto.AdminResponse;
import com.nangoso.pricetracker.entity.Item;
import com.nangoso.pricetracker.entity.ItemPrice;
import com.nangoso.pricetracker.repository.ItemPriceRepository;
import com.nangoso.pricetracker.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 관리자 기능 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ItemRepository itemRepository;
    private final ItemPriceRepository itemPriceRepository;

    @Value("${admin.password}")
    private String adminPassword;

    /**
     * 특정 아이템의 특정 날짜, 특정 가격을 비활성화 처리
     */
    @Transactional
    public AdminResponse disablePrice(AdminPriceDisableRequest request) {
        // 1. 관리자 비밀번호 검증
        if (!adminPassword.equals(request.getAdminPassword())) {
            log.warn("관리자 비밀번호 불일치");
            return AdminResponse.error("관리자 비밀번호가 일치하지 않습니다.");
        }

        // 2. 아이템 존재 여부 확인
        Optional<Item> itemOptional = itemRepository.findByItemCode(request.getItemCode());
        if (itemOptional.isEmpty()) {
            log.warn("아이템 코드를 찾을 수 없음: {}", request.getItemCode());
            return AdminResponse.error("아이템 코드를 찾을 수 없습니다: " + request.getItemCode());
        }

        Item item = itemOptional.get();
        LocalDate date = request.getDate();
        Long price = request.getPrice();

        // 3. 해당 날짜의 모든 가격 데이터 조회
        List<ItemPrice> prices = itemPriceRepository.findAllByItemAndDate(item, date);

        if (prices.isEmpty()) {
            log.warn("해당 날짜에 가격 데이터가 없음. 아이템: {}, 날짜: {}", item.getName(), date);
            return AdminResponse.error("해당 날짜에 가격 데이터가 없습니다.");
        }

        // 4. 해당 가격과 일치하는 데이터 찾기 및 비활성화 처리
        boolean found = false;
        int disabledCount = 0;

        for (ItemPrice itemPrice : prices) {
            if (itemPrice.getPrice().equals(price) && itemPrice.getStatus() == ItemPrice.PriceStatus.ACTIVE) {
                itemPrice.setStatus(ItemPrice.PriceStatus.INACTIVE);
                itemPriceRepository.save(itemPrice);
                found = true;
                disabledCount++;
                log.info("가격 비활성화 처리: 아이템={}, 날짜={}, 가격={}, ID={}", item.getName(), date, price, itemPrice.getId());
            }
        }

        if (!found) {
            log.warn("해당 가격을 찾을 수 없거나 이미 비활성화됨. 아이템: {}, 날짜: {}, 가격: {}", item.getName(), date, price);
            return AdminResponse.error("해당 가격을 찾을 수 없거나 이미 비활성화되었습니다.");
        }

        String message = String.format("%d개의 가격 데이터를 비활성화했습니다. (아이템: %s, 날짜: %s, 가격: %,d원)", disabledCount, item.getName(), date, price);
        log.info(message);
        return AdminResponse.success(message);
    }
}
