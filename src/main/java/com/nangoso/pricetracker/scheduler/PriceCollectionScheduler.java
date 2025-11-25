package com.nangoso.pricetracker.scheduler;

import com.nangoso.pricetracker.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceCollectionScheduler {

    private final ItemService itemService;

    /**
     * 애플리케이션 시작 시 아이템 정보를 초기화합니다.
     * 이미 DB에 아이템이 있으면 스킵합니다.
     * 초기화 후 JSON 파일을 생성합니다.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeItems() {
        log.info("Application ready - Checking if items need to be initialized...");
        try {
            itemService.collectAndSaveItemInfo();
            // 아이템 수집 후 JSON 파일로 내보내기
            itemService.exportItemsToJson();
        } catch (Exception e) {
            log.error("Failed to initialize items", e);
        }
    }

    /**
     * 매일 자정에 모든 아이템의 가격 정보를 수집합니다.
     * cron 표현식: "초 분 시 일 월 요일"
     * 0 30 12 * * * = 매일 정오
     */
    @Scheduled(cron = "15 0 0-23/12 * * *")
    public void collectDailyPrices() {
        log.info("Starting scheduled daily price collection...");
        try {
            itemService.collectAndSavePrices();
        } catch (Exception e) {
            log.error("Failed to collect daily prices", e);
        }
    }

    /**
     * 테스트용: 10분마다 가격 정보 수집 (개발 중에만 사용)
     * 실제 운영 시에는 주석 처리하거나 제거해야 합니다.
     */
//     @Scheduled(fixedRate = 600000) // 10분 = 600,000ms
    public void collectPricesForTesting() {
        log.info("Starting test price collection...");
        try {
            itemService.collectAndSavePrices();
        } catch (Exception e) {
            log.error("Failed to collect prices for testing", e);
        }
    }
}
