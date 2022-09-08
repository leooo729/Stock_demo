package com.example.demo.service;

import static java.lang.Math.floor;

public class CountService {
    public double countAmt(double price, double qty) {
        double amt = floor(price * qty);
        return amt;
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
