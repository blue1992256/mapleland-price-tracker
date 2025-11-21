package com.nangoso.pricetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nangoso.pricetracker.dto.*;
import com.nangoso.pricetracker.entity.Item;
import com.nangoso.pricetracker.entity.ItemPrice;
import com.nangoso.pricetracker.repository.ItemPriceRepository;
import com.nangoso.pricetracker.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemPriceRepository itemPriceRepository;
    private final ItemCodeParserService itemCodeParserService;
    private final WebScrapingService webScrapingService;

    /**
     * TXT 파일에서 추출한 아이템 코드로 아이템 정보를 수집하고 저장합니다.
     * DB에 이미 존재하는 아이템은 스킵하고, 없는 경우에만 웹에서 정보를 가져와 저장합니다.
     */
    @Transactional
    public void collectAndSaveItemInfo() {
        log.info("Starting item info collection...");

        List<String> itemCodes = itemCodeParserService.extractItemCodes();
        log.info("Found {} item codes from TXT file", itemCodes.size());

        int successCount = 0;
        int skipCount = 0;

        for (String itemCode : itemCodes) {
            try {
                // 이미 DB에 존재하는 아이템인지 확인
                if (itemRepository.existsByItemCode(itemCode)) {
                    log.debug("Item already exists in DB: {}", itemCode);
                    skipCount++;
                    continue;
                }

                // 웹에서 아이템 정보 가져오기
                WebScrapingService.ItemInfo itemInfo = webScrapingService.fetchItemInfo(itemCode);

                if (itemInfo == null || itemInfo.getName() == null || itemInfo.getName().isEmpty()) {
                    log.warn("Skipping item code {} - No valid item info found", itemCode);
                    skipCount++;
                    continue;
                }

                // 아이템 정보 저장
                Item item = Item.builder()
                        .itemCode(itemCode)
                        .name(itemInfo.getName())
                        .imageUrl(itemInfo.getImageUrl())
                        .build();

                itemRepository.save(item);
                log.info("Saved item: {} ({})", item.getName(), itemCode);
                successCount++;

                // 크롤링 부하 방지를 위한 딜레이
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Failed to process item code: {}", itemCode, e);
                skipCount++;
            }
        }

        log.info("Item info collection completed - Success: {}, Skipped: {}, Total: {}",
                successCount, skipCount, itemCodes.size());
    }

    /**
     * 모든 아이템의 가격 정보를 수집하고 저장합니다.
     */
    @Transactional
    public void collectAndSavePrices() {
        log.info("Starting daily price collection...");

        List<Item> items = itemRepository.findAll();
        LocalDate today = LocalDate.now();

        int successCount = 0;
        int skipCount = 0;

        for (Item item : items) {
            try {
                // 오늘 날짜의 가격 데이터가 이미 존재하는지 확인
                Long existingCount = itemPriceRepository.countByItemAndDate(item, today);
                if (existingCount > 0) {
                    log.debug("Price data already exists for today: {} ({})", item.getName(), item.getItemCode());
                    skipCount++;
                    continue;
                }

                // 웹에서 판매 가격 목록 가져오기
                List<Long> prices = webScrapingService.fetchSellingPrices(item.getItemCode());

                if (prices.isEmpty()) {
                    log.warn("No prices found for item: {} ({})", item.getName(), item.getItemCode());
                    skipCount++;
                    continue;
                }

                // 각 가격을 개별 레코드로 저장
                for (Long price : prices) {
                    ItemPrice itemPrice = ItemPrice.builder()
                            .item(item)
                            .date(today)
                            .price(price)
                            .build();

                    itemPriceRepository.save(itemPrice);
                }

                log.info("Saved {} prices for item: {} ({})", prices.size(), item.getName(), item.getItemCode());
                successCount++;

                // 크롤링 부하 방지를 위한 딜레이
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Failed to collect prices for item: {} ({})", item.getName(), item.getItemCode(), e);
                skipCount++;
            }
        }

        log.info("Daily price collection completed - Success: {}, Skipped: {}, Total: {}",
                successCount, skipCount, items.size());
    }

    /**
     * DB에 저장된 아이템 목록을 JSON 파일로 내보냅니다.
     */
    public void exportItemsToJson() {
        log.info("Starting item export to JSON...");

        try {
            List<Item> items = itemRepository.findAll();

            // DTO로 변환
            List<ItemExportDto> itemDtos = items.stream()
                    .map(item -> new ItemExportDto(
                            item.getItemCode(),
                            item.getName(),
                            item.getImageUrl()
                    ))
                    .collect(Collectors.toList());

            // JSON 파일로 저장
            ObjectMapper objectMapper = new ObjectMapper();
            File jsonFile = new File("src/main/resources/static/files/items.json");

            // 디렉토리가 없으면 생성
            File parentDir = jsonFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, itemDtos);

            log.info("Successfully exported {} items to {}", itemDtos.size(), jsonFile.getPath());

        } catch (IOException e) {
            log.error("Failed to export items to JSON", e);
        }
    }

    /**
     * 아이템 상세 정보 조회
     * @param itemCode 아이템 코드
     * @return 아이템 상세 DTO (없으면 null)
     */
    public ItemDetailDto getItemDetail(String itemCode) {
        // 아이템 조회
        Optional<Item> itemOptional = itemRepository.findByItemCode(itemCode);
        if (itemOptional.isEmpty()) {
            log.warn("Item not found with itemCode: {}", itemCode);
            return null;
        }

        Item item = itemOptional.get();
        LocalDate today = LocalDate.now();

        // 오늘 가격 통계 조회
        TodayPriceDto todayPrice = getTodayPriceStats(item, today);

        return new ItemDetailDto(
                item.getItemCode(),
                item.getName(),
                item.getImageUrl(),
                item.getCreatedAt().toLocalDate(),
                todayPrice
        );
    }

    /**
     * 아이템의 가격 히스토리 조회
     * @param itemCode 아이템 코드
     * @param days 조회할 일수 (기본 30일)
     * @return 가격 히스토리 목록
     */
    public List<PriceHistoryDto> getPriceHistory(String itemCode, int days) {
        Optional<Item> itemOptional = itemRepository.findByItemCode(itemCode);
        if (itemOptional.isEmpty()) {
            log.warn("Item not found with itemCode: {}", itemCode);
            return new ArrayList<>();
        }

        Item item = itemOptional.get();
        LocalDate startDate = LocalDate.now().minusDays(days);

        // 날짜별 통계 조회
        List<Object[]> stats = itemPriceRepository.findDailyStatsByItemAndDateAfter(item, startDate);

        return stats.stream()
                .map(stat -> {
                    LocalDate date = (LocalDate) stat[0];
                    Double avgPrice = (Double) stat[1];
                    Long minPrice = (Long) stat[2];
                    Long maxPrice = (Long) stat[3];
                    Long count = (Long) stat[4];

                    // 중앙값 계산 (단순화: 평균값 사용)
                    Long medianPrice = avgPrice != null ? avgPrice.longValue() : null;

                    return new PriceHistoryDto(
                            date,
                            avgPrice != null ? avgPrice.longValue() : null,
                            minPrice,
                            maxPrice,
                            medianPrice,
                            count.intValue()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 가격 통계 조회
     */
    private TodayPriceDto getTodayPriceStats(Item item, LocalDate date) {
        Double avgPriceDouble = itemPriceRepository.findAvgPriceByItemAndDate(item, date);
        Long minPrice = itemPriceRepository.findMinPriceByItemAndDate(item, date);
        Long maxPrice = itemPriceRepository.findMaxPriceByItemAndDate(item, date);
        Integer count = itemPriceRepository.findCountByItemAndDate(item, date);

        Long avgPrice = avgPriceDouble != null ? avgPriceDouble.longValue() : null;
        Long medianPrice = avgPrice; // 단순화: 평균값을 중앙값으로 사용

        // 전날 대비 변동률 계산
        Double changeRate = calculateChangeRate(item, date);

        return new TodayPriceDto(
                date,
                avgPrice,
                minPrice,
                maxPrice,
                medianPrice,
                count,
                changeRate
        );
    }

    /**
     * 전날 대비 가격 변동률 계산
     */
    private Double calculateChangeRate(Item item, LocalDate currentDate) {
        LocalDate previousDate = currentDate.minusDays(1);

        Double currentAvg = itemPriceRepository.findAvgPriceByItemAndDate(item, currentDate);
        Double previousAvg = itemPriceRepository.findAvgPriceByItemAndDate(item, previousDate);

        if (currentAvg == null || previousAvg == null || previousAvg == 0) {
            return 0.0;
        }

        return ((currentAvg - previousAvg) / previousAvg) * 100;
    }
}
