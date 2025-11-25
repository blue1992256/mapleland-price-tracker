package com.nangoso.pricetracker.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemInfo {

  private String name;
  private String imageUrl;

}
