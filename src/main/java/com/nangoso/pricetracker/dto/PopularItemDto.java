package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularItemDto {

  private String itemCode;
  private String name;
  private String imageUrl;
  private Long currentAvgPrice;  // 오늘 평균 가격
  private Double priceChangeRate;  // 전날 대비 등락율 (%)

}
