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
    @Column(name = "TradeDate", columnDefinition = "char(8) NOT NULL")
    private String tradeDate;
    @Id
    @Column(name = "BranchNo", columnDefinition = "char(4) NOT NULL")
    private String branchNo;
    @Id
    @Column(name = "CustSeq", columnDefinition = "char(7) NOT NULL")
    private String custSeq;
    @Id
    @Column(name = "DocSeq", columnDefinition = "char(5) NOT NULL")
    private String docSeq;
    @Column(name = "Stock", columnDefinition = "char(6)")
    private String stock;
    @Column(name = "BsType", columnDefinition = "char(1)")
    private String bsType;
    @Column(name = "Price")
    private double price;
    @Column(name = "Qty")
    private Double qty;
    @Column(name = "Amt")
    private double amt;
    @Column(name = "Fee")
    private double fee;
    @Column(name = "Tax")
    private double tax;
    @Column(name = "StinTax")
    private double stinTax;
    @Column(name = "NetAmt", columnDefinition = "decimal(16,2)")
    private double netAmt;
    @Column(name = "ModDate", columnDefinition = "char(8)")
    private String modDate;
    @Column(name = "ModTime", columnDefinition = "char(6)")
    private String modTime;
    @Column(name = "ModUser", columnDefinition = "char(10)")
    private String modUser;

    public double countAmt(double price, double qty) {
        double amt = floor(price * qty);
        return amt;
    }

    public String makeDocSeq() {
        int alphabetCount = 26;
        alphabetCount++;
//        num++;
//        if(num<10){
//            String s=Integer.toString(num);
//            String numToString="00".concat(s);
//        }
        //String numToString =Integer.toString(num);
        return str(alphabetCount);
    }

    public String str(int alphabetCount) {
        String alphabet = alphabetCount < 0 ? "" : str((alphabetCount / 26) - 1) + (char) (65 + alphabetCount % 26);
        return alphabet;
    }

    public double countFee(double amt) {
        double fee = floor((amt * 0.001425));
        return fee;
    }

    public double countTax(double amt, String bsType) {
        if ("S".equals(bsType)) {
            double tax = floor((amt * 0.003));
            return tax;
        } else if ("B".equals(bsType)) {
            return 0;
        }
        return 0;
    }

    public double countNetAmt(double amt, String bsType, double fee, double tax) {
        if ("S".equals(bsType)) {
            double netamt = floor((amt - fee - tax));
            return netamt;
        } else if ("B".equals(bsType)) {
            double netamt = -floor((amt + fee));
            return netamt;
        }
        return 0;
    }





    }


