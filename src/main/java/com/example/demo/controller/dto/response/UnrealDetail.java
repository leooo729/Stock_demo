package com.example.demo.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnrealDetail {
    private String tradeDate;
    private String docSeq;
    private String stock;
    private String stockName;
    private String buyPrice;
    private String nowPrice;
    private Long qty;
    private Long remainQty;
    private Integer fee;
    private Integer cost;
    private Long marketValue;
    private Long unrealProfit;
    private String profitability;
}
