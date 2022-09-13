package com.example.demo.model.entity;

import com.example.demo.model.HcmioRepository;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;

import static java.lang.Math.floor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hcmio")
@IdClass(HcmioRelationPK.class)

public class Hcmio {

    @Id
    @Column(name = "TradeDate")
    private String tradeDate;
    @Id
    @Column(name = "BranchNo")
    private String branchNo;
    @Id
    @Column(name = "CustSeq")
    private String custSeq;
    @Id
    @Column(name = "DocSeq")
    private String docSeq;
    @Column(name = "Stock")
    private String stock;
    @Column(name = "BsType")
    private String bsType;
    @Column(name = "Price")
    private Double price;
    @Column(name = "Qty")
    private Double qty;
    @Column(name = "Amt")
    private Double amt;
    @Column(name = "Fee")
    private Double fee;
    @Column(name = "Tax")
    private Double tax;
    @Column(name = "StinTax")
    private Double stinTax;
    @Column(name = "NetAmt")
    private Double netAmt;
    @Column(name = "ModDate")
    private String modDate;
    @Column(name = "ModTime")
    private String modTime;
    @Column(name = "ModUser")
    private String modUser;

    }


