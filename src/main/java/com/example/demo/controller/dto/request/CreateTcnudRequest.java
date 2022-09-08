package com.example.demo.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTcnudRequest {
    private String tradeDate;
    private String branchNo;
    private String custSeq;
    private String docSeq;
    private String stock;
    private double price;
    private double qty;
    private double remainQty;
    private double fee;
    private double cost;
    private String modDate;
    private String modTime;
    private String modUser;
}
