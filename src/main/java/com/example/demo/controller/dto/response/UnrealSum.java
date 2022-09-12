package com.example.demo.controller.dto.response;

import com.example.demo.controller.dto.response.UnrealDetail;
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
    private Double nowPrice;
    private Double sumRemainQty;
    private Double sumFee;
    private Double sumCost;
    private Double sumMarketValue;
    private Double sumUnrealProfit;
    private List<UnrealDetail> detaiList;
}
