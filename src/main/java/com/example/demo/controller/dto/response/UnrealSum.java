package com.example.demo.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UnrealSum {
    private String stock;
    private String stockName;
    private String nowPrice;
    private Long sumRemainQty;
    private Integer sumFee;
    private Integer sumCost;
    private Long sumMarketValue;
    private Long sumUnrealProfit;
    private String profitability;
    private List<UnrealDetail> detaiList;

}
