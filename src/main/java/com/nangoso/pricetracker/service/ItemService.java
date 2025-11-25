package com.nangoso.pricetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nangoso.pricetracker.dto.ItemDetailDto;
import com.nangoso.pricetracker.dto.ItemExportDto;
import com.nangoso.pricetracker.dto.ItemInfo;
import com.nangoso.pricetracker.dto.PopularItemDto;
import com.nangoso.pricetracker.dto.PriceData;
import com.nangoso.pricetracker.dto.PriceHistoryDto;
import com.nangoso.pricetracker.dto.TodayPriceDto;
import com.nangoso.pricetracker.entity.Item;
import com.nangoso.pricetracker.entity.ItemPrice;
import com.nangoso.pricetracker.repository.ItemPriceRepository;
import com.nangoso.pricetracker.repository.ItemRepository;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                ItemInfo itemInfo = webScrapingService.fetchItemInfo(itemCode);

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
                        .viewCount(0L)
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
     * 같은 날짜, 같은 URL의 데이터는 중복으로 간주하여 스킵합니다.
     */
    @Transactional
    public void collectAndSavePrices() {
        log.info("Starting price collection...");

        List<Item> items = itemRepository.findAll();
        LocalDate today = LocalDate.now();

        int successCount = 0;
        int skipCount = 0;
        int newDataCount = 0;
        int duplicateCount = 0;

        for (Item item : items) {
            try {
                // 웹에서 판매 가격과 URL 목록 가져오기
                List<PriceData> priceDataList = webScrapingService.fetchSellingPricesWithUrl(item.getItemCode());

                if (priceDataList.isEmpty()) {
                    log.warn("No prices found for item: {} ({})", item.getName(), item.getItemCode());
                    skipCount++;
                    continue;
                }

                // 가격만 추출하여 IQR 방식으로 이상치 판별
                List<Long> prices = priceDataList.stream()
                        .map(PriceData::getPrice)
                        .collect(Collectors.toList());

                List<Long> validPrices = removeOutliersUsingIQR(prices);

                // 각 가격 데이터를 확인하여 중복이 아닌 경우에만 저장
                int itemNewCount = 0;
                int itemDuplicateCount = 0;
                int itemInactiveCount = 0;

                for (PriceData priceData : priceDataList) {
                    // 같은 날짜, 같은 URL이 이미 존재하는지 확인
                    boolean exists = itemPriceRepository.existsByItemAndDateAndUrl(item, today, priceData.getUrl());

                    if (exists) {
                        itemDuplicateCount++;
                        continue;
                    }

                    // IQR 검증 결과에 따라 상태 결정
                    boolean isValid = validPrices.contains(priceData.getPrice());

                    // 확붙, 붙펑 인 경우 코멘트 필터로 상태 결정
                    boolean isValid2 = itemFilter(priceData.getComment());

                    ItemPrice.PriceStatus status = (isValid && isValid2) ? ItemPrice.PriceStatus.ACTIVE : ItemPrice.PriceStatus.INACTIVE;

                    // 모든 데이터를 저장 (이상치는 INACTIVE 상태로)
                    ItemPrice itemPrice = ItemPrice.builder()
                            .item(item)
                            .date(today)
                            .price(priceData.getPrice())
                            .url(priceData.getUrl())
                            .status(status)
                            .build();

                    itemPriceRepository.save(itemPrice);

                    if (isValid) {
                        itemNewCount++;
                    } else {
                        itemInactiveCount++;
                    }
                }

                newDataCount += itemNewCount;
                duplicateCount += itemDuplicateCount;

                if (itemNewCount > 0 || itemInactiveCount > 0) {
                    log.info("Saved {} ACTIVE and {} INACTIVE prices for item: {} ({}) - {} duplicates skipped",
                            itemNewCount, itemInactiveCount, item.getName(), item.getItemCode(), itemDuplicateCount);
                    successCount++;
                } else {
                    log.debug("No new prices for item: {} ({}) - {} duplicates",
                            item.getName(), item.getItemCode(), itemDuplicateCount);
                }

                // 크롤링 부하 방지를 위한 딜레이
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Failed to collect prices for item: {} ({})", item.getName(), item.getItemCode(), e);
                skipCount++;
            }
        }

        log.info("Price collection completed - Items processed: {}, ACTIVE records: {}, Duplicates skipped: {}, Items skipped: {}, Total items: {}",
                successCount, newDataCount, duplicateCount, skipCount, items.size());
    }

    public List<Long> removeOutliersUsingIQR(List<Long> values) {
      if (values == null || values.size() < 4) {
        return values; // 데이터가 너무 적으면 IQR 사용이 의미 없음
      }

      // 1. Long -> Double 변환 후 정렬
      List<Double> doubleValues = values.stream()
              .map(Long::doubleValue)
              .sorted()
              .collect(Collectors.toList());

      // 2. Q1, Q3 계산
      double q1 = getPercentile(doubleValues, 25);
      double q3 = getPercentile(doubleValues, 75);
      double iqr = q3 - q1;

      // 3. IQR 기준으로 최소/최대 경계 계산
      double lowerBound = q1 - 1.5 * iqr;
      double upperBound = q3 + 1.5 * iqr;

      // 4. 경계 안에 들어오는 값만 필터링 후 Long으로 변환
      List<Long> filtered = values.stream()
              .filter(v -> v >= lowerBound && v <= upperBound)
              .collect(Collectors.toList());

      return filtered;
    }

    // 퍼센타일 구하기 (선형 보간법 없이 단순 계산)
    private double getPercentile(List<Double> sorted, double percentile) {
      if (sorted.isEmpty()) return 0;

      double index = percentile / 100.0 * (sorted.size() - 1);
      int i = (int) index;
      double fraction = index - i;

      if (i + 1 < sorted.size()) {
        return sorted.get(i) + (sorted.get(i + 1) - sorted.get(i)) * fraction;
      } else {
        return sorted.get(i);
      }
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
     * 기존에 저장된 모든 가격 데이터에 대해 IQR 검증을 수행하고 이상치를 INACTIVE 상태로 변경합니다.
     */
    @Transactional
    public void validateAndUpdateExistingPrices() {
        log.info("Starting validation of existing price data...");

        List<Item> items = itemRepository.findAll();
        int totalProcessed = 0;
        int totalInactivated = 0;

        for (Item item : items) {
            try {
                // 아이템별로 날짜를 그룹화하여 처리
                List<ItemPrice> allPrices = itemPriceRepository.findByItemOrderByDateDesc(item);

                // 날짜별로 그룹화
                var pricesByDate = allPrices.stream()
                        .collect(Collectors.groupingBy(ItemPrice::getDate));

                for (var entry : pricesByDate.entrySet()) {
                    LocalDate date = entry.getKey();
                    List<ItemPrice> dailyPrices = entry.getValue();

                    if (dailyPrices.size() < 4) {
                        log.debug("Skipping validation for {} ({}) on {} - insufficient data ({})",
                                item.getName(), item.getItemCode(), date, dailyPrices.size());
                        continue;
                    }

                    // 가격 값만 추출
                    List<Long> prices = dailyPrices.stream()
                            .map(ItemPrice::getPrice)
                            .collect(Collectors.toList());

                    // IQR로 필터링된 가격 계산
                    List<Long> validPrices = removeOutliersUsingIQR(prices);

                    // 이상치 판별 및 상태 업데이트
                    int inactivatedCount = 0;
                    for (ItemPrice itemPrice : dailyPrices) {
                        boolean isValid = validPrices.contains(itemPrice.getPrice());

                        if (!isValid && itemPrice.getStatus() == ItemPrice.PriceStatus.ACTIVE) {
                            itemPrice.setStatus(ItemPrice.PriceStatus.INACTIVE);
                            itemPriceRepository.save(itemPrice);
                            inactivatedCount++;
                            totalInactivated++;
                        }
                    }

                    if (inactivatedCount > 0) {
                        log.info("Updated {} ({}) on {} - {} outliers marked as INACTIVE",
                                item.getName(), item.getItemCode(), date, inactivatedCount);
                    }

                    totalProcessed++;
                }

            } catch (Exception e) {
                log.error("Failed to validate prices for item: {} ({})", item.getName(), item.getItemCode(), e);
            }
        }

        log.info("Validation completed - Processed: {} item-dates, Total inactivated: {}",
                totalProcessed, totalInactivated);
    }

    /**
     * 인기 아이템 조회 (조회수 기준 상위 N개)
     * @param limit 조회할 아이템 개수
     * @return 인기 아이템 목록
     */
    public List<PopularItemDto> getPopularItems(int limit) {
        List<Item> popularItems = itemRepository.findAllOrderByViewCountDesc(limit);
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        return popularItems.stream()
                .map(item -> {
                    // 오늘 평균 가격 조회
                    Double todayAvg = itemPriceRepository.findAvgPriceByItemAndDate(item, today);

                    // 전날 평균 가격 조회
                    Double yesterdayAvg = itemPriceRepository.findAvgPriceByItemAndDate(item, yesterday);

                    // 등락율 계산
                    Double priceChangeRate = 0.0;
                    if (todayAvg != null && yesterdayAvg != null && yesterdayAvg > 0) {
                        priceChangeRate = ((todayAvg - yesterdayAvg) / yesterdayAvg) * 100;
                    }

                    return PopularItemDto.builder()
                            .itemCode(item.getItemCode())
                            .name(item.getName())
                            .imageUrl(item.getImageUrl())
                            .currentAvgPrice(todayAvg != null ? todayAvg.longValue() : null)
                            .priceChangeRate(priceChangeRate)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 아이템 상세 정보 조회
     * @param itemCode 아이템 코드
     * @return 아이템 상세 DTO (없으면 null)
     */
    @Transactional
    public ItemDetailDto getItemDetail(String itemCode) {
        // 조회수 증가 (Repository 직접 호출로 변경)
        itemRepository.incrementViewCount(itemCode);

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

    /**
     * 판매 상품 코멘트로 필터
     */
    private boolean itemFilter(String comment) {
      if (comment.contains("확") || comment.contains("붙") || comment.contains("펑")) {
        return false;
      }
      return true;
    }
}
