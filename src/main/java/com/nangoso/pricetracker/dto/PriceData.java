package com.nangoso.pricetracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceData {

  private Long price;
  private String url;
  private String comment;
  private String status;

}
