package com.nangoso.pricetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nangoso.pricetracker.dto.ItemInfo;
import com.nangoso.pricetracker.dto.PriceData;
import com.nangoso.pricetracker.dto.TradeItem;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WebScrapingService {

  private static final String API_BASE_URL = "https://api.mapleland.gg/trade?itemCode=";
  private static final String IMAGE_BASE_URL = "https://maplestory.io/api/gms/200/item/";
  private static final int TIMEOUT_SECONDS = 10;

  private final ObjectMapper objectMapper;

  public WebScrapingService() {
    this.objectMapper = new ObjectMapper();
  }

  /**
   * 아이템 정보를 API에서 가져옵니다.
   */
  public ItemInfo fetchItemInfo(String itemCode) {
    String apiUrl = API_BASE_URL + itemCode;
    log.info("Fetching item info from API (OkHttp): {}", apiUrl);

    OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .build();

    Request request = new Request.Builder()
        .url(apiUrl)
        .get()
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0 Safari/537.36")
        .header("Accept", "application/json, text/plain, */*")
        .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
        .header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache")
        .header("Connection", "keep-alive")
        .header("Origin", "https://mapleland.gg")
        .header("Referer", "https://mapleland.gg/")
        .header("Sec-Fetch-Site", "same-origin")
        .header("Sec-Fetch-Mode", "cors")
        .header("Sec-Fetch-Dest", "empty")
        .build();

    try (Response response = client.newCall(request).execute()) {

      if (!response.isSuccessful()) {
        log.warn("Failed to fetch item info for code: {} - Status: {}",
            itemCode, response.code());
        return null;
      }

      String body = response.body().string();

      // JSON 파싱
      TradeItem[] tradeItems = objectMapper.readValue(body, TradeItem[].class);

      if (tradeItems == null || tradeItems.length == 0) {
        log.warn("No trade items found for code: {}", itemCode);
        return null;
      }

      TradeItem firstItem = tradeItems[0];
      String itemName = firstItem.getItemName();

      // 이미지 URL 생성
      String imageUrl = IMAGE_BASE_URL + itemCode + "/icon?resize=2";

      log.info("Item info fetched - Name: {}, Image: {}", itemName, imageUrl);

      return ItemInfo.builder()
          .name(itemName)
          .imageUrl(imageUrl)
          .build();

    } catch (Exception e) {
      log.error("Failed to fetch item info for code: {}", itemCode, e);
      return null;
    }
  }

  /**
   * 판매 가격과 URL을 함께 가져옵니다 (tradeType이 "sell"인 것만)
   */
  public List<PriceData> fetchSellingPricesWithUrl(String itemCode) {
    String apiUrl = API_BASE_URL + itemCode;
    log.info("Fetching selling prices with URL from API (OkHttp): {}", apiUrl);

    List<PriceData> priceDataList = new ArrayList<>();

    OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .build();

    Request request = new Request.Builder()
        .url(apiUrl)
        .get()
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0 Safari/537.36")
        .header("Accept", "application/json, text/plain, */*")
        .header("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.8")
        .header("Cache-Control", "no-cache")
        .header("Pragma", "no-cache")
        .header("Connection", "keep-alive")
        .header("Origin", "https://mapleland.gg")
        .header("Referer", "https://mapleland.gg/")
        .header("Sec-Fetch-Site", "same-origin")
        .header("Sec-Fetch-Mode", "cors")
        .header("Sec-Fetch-Dest", "empty")
        .build();

    try (Response response = client.newCall(request).execute()) {

      if (!response.isSuccessful()) {
        log.warn("Failed to fetch prices for code: {} - Status: {}",
            itemCode, response.code());
        return priceDataList;
      }

      String body = response.body().string();

      // JSON 파싱
      TradeItem[] tradeItems = objectMapper.readValue(body, TradeItem[].class);

      if (tradeItems == null || tradeItems.length == 0) {
        log.warn("No trade items found for code: {}", itemCode);
        return priceDataList;
      }

      // "sell"인 거래만 수집
      for (TradeItem item : tradeItems) {
        if ("sell".equalsIgnoreCase(item.getTradeType()) && item.isTradeStatus()) {
          priceDataList.add(
              new PriceData(item.getItemPrice(), item.getUrl(), item.getComment(), null)
          );
        }
      }

      log.info("Fetched {} selling prices with URL for item code: {}",
          priceDataList.size(), itemCode);

    } catch (Exception e) {
      log.error("Failed to fetch selling prices for code: {}", itemCode, e);
    }

    return priceDataList;
  }

}
