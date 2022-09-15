package com.example.demo.service;

import com.example.demo.controller.dto.request.TransactionRequest;
import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.UnrealDetail;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.TcnudRepository;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.lang.Math.round;

@Service
public class TransactionMethodService {

    @Autowired
    TcnudRepository tcnudRepository;
    @Autowired
    MstmbRepository mstmbRepository;

    public UnrealDetail getUnrealDetail(Tcnud tcnud) { //取得未實現損益明細的物件
        UnrealDetail unrealDetail = new UnrealDetail(); //先創建一未實現損益明細的物件
        Mstmb mstmb = mstmbRepository.findByStock(tcnud.getStock()); //找到改檔股票明細檔

        unrealDetail.setTradeDate(tcnud.getTradeDate());
        unrealDetail.setDocSeq(tcnud.getDocSeq());
        unrealDetail.setStock(tcnud.getStock());
        unrealDetail.setStockName(mstmb.getStockName()); //明細檔抓股票名稱
        unrealDetail.setBuyPrice(String.format("%.2f", makeRoundTwo(tcnud.getPrice())));
        unrealDetail.setNowPrice(String.format("%.2f", makeRoundTwo(mstmb.getCurPrice())));//明細檔抓股票現值
        unrealDetail.setQty(tcnud.getQty());
        unrealDetail.setRemainQty(tcnud.getRemainQty());
        unrealDetail.setFee(countFee(countAmt(tcnud.getPrice(), tcnud.getRemainQty())));
        unrealDetail.setCost(tcnud.getCost());
        unrealDetail.setMarketValue(countMarketValue(unrealDetail.getRemainQty(), mstmb.getCurPrice()));
        unrealDetail.setUnrealProfit(countUnrealProfit(unrealDetail.getMarketValue(), unrealDetail.getCost()));
        unrealDetail.setProfitability(String.format("%.2f", makeRoundTwo(countProfitability(unrealDetail.getUnrealProfit(), unrealDetail.getCost()))) + "%");
        return unrealDetail;
    }

    public String checkTransactionRequest(TransactionRequest transactionRequest) { //檢查傳入的交易資料是否都正確
        if (transactionRequest.getTradeDate().isBlank()||8!=transactionRequest.getTradeDate().length()) {
            return "交易日期輸入錯誤(長度需為8碼)";
        }
        if (transactionRequest.getBranchNo().isBlank() || 4!=transactionRequest.getBranchNo().length()) {
            return "分行代碼輸入錯誤(長度需為4碼)";
        }
        if (transactionRequest.getCustSeq().isBlank() || 2!=transactionRequest.getCustSeq().length()) {
            return "客戶帳號輸入錯誤(長度需為2碼)";
        }
        if (transactionRequest.getDocSeq().isBlank()|| 5!=transactionRequest.getDocSeq().length()) {
            return "委託書號輸入錯誤(長度需為5碼)";
        }
        if (null != tcnudRepository.findByTradeDateAndBranchNoAndCustSeqAndDocSeq(transactionRequest.getTradeDate(), transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getDocSeq())) {
            return "輸入資料已存在";
        }
        if (transactionRequest.getStock().isBlank()||4!=transactionRequest.getStock().length()) {
            return "股票代碼輸入錯誤(長度需為4碼)";
        }
        if (null == mstmbRepository.findByStock(transactionRequest.getStock())) {
            return "無此檔股票資料";
        }
        if (transactionRequest.getPrice() <= 0 || null == transactionRequest.getPrice()) {
            return "請輸入有效購買價格";
        }
        if(transactionRequest.getPrice()>=1_000_000){
            return "輸入價格超出範圍";
        }
        if (transactionRequest.getQty() <= 0 || null == transactionRequest.getQty()) {
            return "請輸入有效購買數量";
        }
        if(transactionRequest.getQty()>=1_000_000_000){
            return "輸入數量超出範圍";
        }
        return "allPass";
    }

    public String checkUnrealRequest(UnrealRequest unrealRequest) { //檢查傳入的查詢未實現損益資料是否都正確
        if (unrealRequest.getBranchNo().isBlank()|| 4!=unrealRequest.getBranchNo().length()) {
            return "分行代碼輸入錯誤(長度需為4碼)";
        }
        if (unrealRequest.getCustSeq().isBlank()||2!=unrealRequest.getCustSeq().length()) {
            return "客戶帳號輸入錯誤(長度需為2碼)";
        }
        if (tcnudRepository.findByBranchNoAndCustSeq(unrealRequest.getBranchNo(), unrealRequest.getCustSeq()).isEmpty()) {
            return "查無符合資料";
        }
        if(!unrealRequest.getStock().isBlank()&&4!=unrealRequest.getStock().length()){
            return "股票代碼輸入錯誤(長度需為4碼)";
        }
        if (null != unrealRequest.getProfitabilityLowerLimit() && null != unrealRequest.getProfitabilityUpperLimit()) {
            if (unrealRequest.getProfitabilityLowerLimit() > unrealRequest.getProfitabilityUpperLimit()) {
                return "上下限範圍輸入錯誤";
            }
        }
        if (!tcnudRepository.findByBranchNoAndCustSeq(unrealRequest.getBranchNo(), unrealRequest.getCustSeq()).isEmpty() && unrealRequest.getStock().isBlank()) {
            return "allPass";
        }
        if (null == mstmbRepository.findByStock(unrealRequest.getStock())) {
            return "無此檔股票資料";
        }
        return "allPass";
    }

    public double countAmt(double price, Long qty) {
        return round(price * qty);
    }

    public Integer countFee(double amt) {
        return (int) round((amt * 0.001425));
    }

    public Integer countTax(double amt, String bsType) {
        return ("S".equals(bsType)) ? (int) round((amt * 0.003)) : 0;
    }

    public Long countNetAmt(double amt, String bsType, double fee, double tax) {
        return ("S".equals(bsType)) ? round((amt - fee - tax)) : -round((amt + fee));
    }

    public Long countUnrealProfit(double marketValue, Integer cost) {
        return round(marketValue - cost);
    }

    public Long countMarketValue(double Qty, double Price) {
        return round(Qty * Price - Qty * Price * 0.003 - Qty * Price * 0.001425);
    }

    public double countProfitability(double unrealProfit, double cost) {
        return unrealProfit / cost * 100;
    }

    public double makeRoundTwo(double number) {
        return round(number * 100) / 100.0;
    }
}

//    private String makeLastDocSeq(String tradeDate) { //自動產生委託書號
//
//        String lastDocSeq = hcmioRepository.getLastDocSeq(tradeDate);
//        if (null == lastDocSeq) {
//            return "AA001";
//        }
//        int firstEngToAscii = lastDocSeq.charAt(0);
//        int secondEngToAscii = lastDocSeq.charAt(1);
//        String num = lastDocSeq.substring(2, 5);
//        int numToInt = Integer.parseInt(num) + 1;
//
//
//        if (numToInt > 999) {
//            numToInt = 1;
//            secondEngToAscii++;
//            if (secondEngToAscii > 90) {
//                secondEngToAscii = 65;
//                firstEngToAscii++;
//            }
//        }
//        String numToString = String.format("%03d", numToInt);
//        String firstEngToString = Character.toString((char) firstEngToAscii);
//        String secondEngToString = Character.toString((char) secondEngToAscii);
//
//        return firstEngToString + secondEngToString + numToString;
//    }

//check
//檢查是否有傳入值以及是否為正確格式
//如有任一欄位未填，回傳有誤
//        if (request.getDocSeq().isBlank() || request.getDocSeq().isEmpty() ||request.getCustSeq().isBlank() || request.getCustSeq().isEmpty() ||request.getBranchNo().isBlank() || request.getBranchNo().isEmpty() ||request.getTradeDate().isBlank() || request.getTradeDate().isEmpty() ||request.getStock().isBlank() || request.getStock().isEmpty() || null == request.getQty()||null==request.getPrice()) {
//                return "表單未填寫完成";
//                }
//                //輸入股票代碼在股票資訊檔中查不到，回傳有誤
//                if (null == mstmbRepository.findByStock(request.getStock())) {
//                return "未知股票";
//                }
//                //如輸入買賣行為(B,S)為小寫，轉成大寫格式，以供判讀
////        if (request.getBsType().equals("s") || request.getBsType().equals("b")) {
////            request.setBsType(request.getBsType().toUpperCase());
////        }
////        //買為行為輸入(b ,s ,B ,S)之外值，回傳有誤
////        if (!request.getBsType().equals("S") && !request.getBsType().equals("B")) {
////            return "需輸入B(買)或S(賣) (大小寫無影響)";
////        }
//                //如輸入不符合數量格式(數量<0 或 等於０)，回傳有誤
//                if (request.getQty() <= 0) {
//                return "請輸入有效股票數量";
//                }
//如檔股票確定有該檔股票餘額，但賣出的股數大於剩餘股數，回傳有誤
//        if (null != tcnudRepository.findByStock(request.getStock())) {
//            if ("S".equals(request.getBsType()) && tcnudRepository.findByStock(request.getStock()).getRemainQty() - request.getQty() < 0) {
//                return "賣出數量超過手上持股，請輸入持股範圍：1 ~ " + String.format("%.0f", (tcnudRepository.findByStock(request.getStock()).getRemainQty()));
//            }
//        }
//        //如果餘額表無該檔股票餘額，且使用者還執行賣出動作，回傳有誤
//        if (null == tcnudRepository.findByStock(request.getStock()) && "S".equals(request.getBsType())) {
//            return "無該檔持股";
//        }
//通過以上檢查，確定傳入的stock,bsType,qty都是有值且正確的
