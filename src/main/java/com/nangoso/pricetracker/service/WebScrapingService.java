package com.nangoso.pricetracker.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WebScrapingService {

    private static final String API_BASE_URL = "https://api.mapleland.gg/trade?itemCode=";
    private static final String IMAGE_BASE_URL = "https://maplestory.io/api/gms/200/item/";
    private static final int TIMEOUT_SECONDS = 10;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public WebScrapingService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 아이템 정보를 API에서 가져옵니다.
     */
    public ItemInfo fetchItemInfo(String itemCode) {
        String apiUrl = API_BASE_URL + itemCode;
        log.info("Fetching item info from API: {}", apiUrl);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://mapleland.gg/")
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Failed to fetch item info for code: {} - Status: {}", itemCode, response.statusCode());
                return null;
            }

            // JSON 파싱
            TradeItem[] tradeItems = objectMapper.readValue(response.body(), TradeItem[].class);

            if (tradeItems == null || tradeItems.length == 0) {
                log.warn("No trade items found for code: {}", itemCode);
                return null;
            }

            // 첫 번째 아이템에서 정보 추출
            TradeItem firstItem = tradeItems[0];
            String itemName = firstItem.getItemName();

            // 이미지 URL 생성 (maplestory.io API 사용)
            String imageUrl = IMAGE_BASE_URL + itemCode + "/icon?resize=2";

            log.info("Item info fetched - Name: {}, Image: {}", itemName, imageUrl);

            return ItemInfo.builder()
                    .name(itemName)
                    .imageUrl(imageUrl)
                    .build();

        } catch (IOException | InterruptedException e) {
            log.error("Failed to fetch item info for code: {}", itemCode, e);
            return null;
        }
    }

    /**
     * 판매 가격 목록을 API에서 가져옵니다 (tradeType이 "sell"인 것만)
     */
    public List<Long> fetchSellingPrices(String itemCode) {
        String apiUrl = API_BASE_URL + itemCode;
        log.info("Fetching selling prices from API: {}", apiUrl);

        List<Long> prices = new ArrayList<>();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Referer", "https://mapleland.gg/")
                    .timeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Failed to fetch prices for code: {} - Status: {}", itemCode, response.statusCode());
                return prices;
            }

            // JSON 파싱
            TradeItem[] tradeItems = objectMapper.readValue(response.body(), TradeItem[].class);

            if (tradeItems == null || tradeItems.length == 0) {
                log.warn("No trade items found for code: {}", itemCode);
                return prices;
            }

            // "sell" 타입(팝니다)인 거래만 가격 수집
            for (TradeItem item : tradeItems) {
                if ("sell".equalsIgnoreCase(item.getTradeType()) && item.isTradeStatus()) {
                    prices.add(item.getItemPrice());
                }
            }

            log.info("Fetched {} selling prices for item code: {}", prices.size(), itemCode);

        } catch (IOException | InterruptedException e) {
            log.error("Failed to fetch selling prices for code: {}", itemCode, e);
        }

        return prices;
    }

    /**
     * API 응답 DTO
     */
    @lombok.Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TradeItem {
        @JsonProperty("itemName")
        private String itemName;

        @JsonProperty("itemCode")
        private Integer itemCode;

        @JsonProperty("itemPrice")
        private Long itemPrice;

        @JsonProperty("tradeType")
        private String tradeType; // "buy" 또는 "sell"

        @JsonProperty("tradeStatus")
        private boolean tradeStatus; // 거래 활성 여부
    }

    /**
     * 아이템 정보를 담는 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ItemInfo {
        private String name;
        private String imageUrl;
    }
}
