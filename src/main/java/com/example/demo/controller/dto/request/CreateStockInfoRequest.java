package com.example.demo.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockInfoRequest {
    private String stock;
    private String stockName;
    private String marketType;
    private Double curPrice;
    private Double refPrice;
    private String currency;
    private String modDate;
    private String modTime;
    private String modUser;
}
