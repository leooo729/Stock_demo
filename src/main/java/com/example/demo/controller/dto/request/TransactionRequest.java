package com.example.demo.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {

    private String tradeDate;
    private String branchNo;
    private String custSeq;
    private String docSeq;
    private String stock;
    private String bsType;
    private Double price;
    private Long qty;
//    private double amt;
//    private double fee;
//    private double tax;
//    private double stinTax;
//    private double netAmt;
//    private String modDate;
//    private String modTime;
//    private String modUser;
}
