package com.nangoso.pricetracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeItem {

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

  @JsonProperty("url")
  private String url;

  @JsonProperty("comment")
  private String comment;

}
