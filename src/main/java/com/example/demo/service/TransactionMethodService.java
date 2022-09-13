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

import static java.lang.Math.floor;
import static java.lang.Math.round;

@Service
public class TransactionMethodService {

    @Autowired
    TcnudRepository tcnudRepository;
    @Autowired
    MstmbRepository mstmbRepository;
    @Autowired
    TransactionMethodService transactionMethodService;

    public UnrealDetail getUnrealDetail(Tcnud tcnud) { //取得未實現損益明細的物件
        UnrealDetail unrealDetail = new UnrealDetail(); //先創建一未實現損益明細的物件
        Mstmb mstmb = mstmbRepository.findByStock(tcnud.getStock()); //找到改檔股票明細檔

        unrealDetail.setTradeDate(tcnud.getTradeDate());
        unrealDetail.setDocSeq(tcnud.getDocSeq());
        unrealDetail.setStock(tcnud.getStock());
        unrealDetail.setStockName(mstmb.getStockName()); //明細檔抓股票名稱
        unrealDetail.setBuyPrice(tcnud.getPrice());
        unrealDetail.setNowPrice(mstmb.getCurPrice());//明細檔抓股票現值
        unrealDetail.setQty(tcnud.getQty());
        unrealDetail.setRemainQty(tcnud.getRemainQty());
        unrealDetail.setFee(tcnud.getRemainQty() * tcnud.getPrice() * 0.001425);
        unrealDetail.setCost(tcnud.getCost());
        unrealDetail.setMarketValue(unrealDetail.getRemainQty() * unrealDetail.getNowPrice() - unrealDetail.getRemainQty() * unrealDetail.getNowPrice() * 0.003 - unrealDetail.getRemainQty() * unrealDetail.getNowPrice() * 0.001425);
        unrealDetail.setUnrealProfit(unrealDetail.getMarketValue() - unrealDetail.getCost());
        unrealDetail.setProfitability((countProfitability(unrealDetail.getUnrealProfit(),unrealDetail.getCost())+"%"));
        return unrealDetail;
    }

    public String checkTransactionRequest(TransactionRequest transactionRequest) { //檢查傳入的交易資料是否都正確
        if (transactionRequest.getTradeDate().isBlank()) {
            return "交易日期輸入錯誤";
        }
        if (transactionRequest.getBranchNo().isBlank()) {
            return "分行代碼輸入錯誤";
        }
        if (transactionRequest.getCustSeq().isBlank()) {
            return "客戶帳號輸入錯誤";
        }
        if (transactionRequest.getDocSeq().isBlank()) {
            return "委託書號輸入錯誤";
        }
        if (null != tcnudRepository.findByTradeDateAndBranchNoAndCustSeqAndDocSeq(transactionRequest.getTradeDate(), transactionRequest.getBranchNo(), transactionRequest.getCustSeq(), transactionRequest.getDocSeq())) {
            return "輸入資料已存在";
        }
        if (transactionRequest.getStock().isBlank()) {
            return "股票代碼輸入錯誤";
        }
        if (null == mstmbRepository.findByStock(transactionRequest.getStock())) {
            return "無此檔股票資料";
        }
        if (transactionRequest.getPrice() <= 0 || null == transactionRequest.getPrice()) {
            return "請輸入有效購買價格";
        }
        if (transactionRequest.getQty() <= 0 || null == transactionRequest.getQty()) {
            return "請輸入有效購買數量";
        }
        return "allPass";
    }

    public String checkUnrealRequest(UnrealRequest unrealRequest) { //檢查傳入的查詢未實現損益資料是否都正確
        if (unrealRequest.getBranchNo().isBlank()) {
            return "分行代碼輸入錯誤";
        }
        if (unrealRequest.getCustSeq().isBlank()) {
            return "客戶帳號輸入錯誤";
        }
        if (!tcnudRepository.findByBranchNoAndCustSeq(unrealRequest.getBranchNo(), unrealRequest.getCustSeq()).isEmpty() && unrealRequest.getStock().isBlank()) {
            return "allPass";
        }
        if (null == mstmbRepository.findByStock(unrealRequest.getStock())) {
            return "無此檔股票資料";
        }
        if (tcnudRepository.findByBranchNoAndCustSeqAndStock(unrealRequest.getBranchNo(), unrealRequest.getCustSeq(),unrealRequest.getStock()).isEmpty()) {
            return "查無符合資料";
        }
        return "allPass";
    }

    public Double countAmt(Double price, Double qty) {
        double amt = round(price * qty);
        return amt;
    }

    public Double countFee(double amt) {
        double fee = round((amt * 0.001425));
        return fee;
    }

    public Double countTax(double amt, String bsType) {
        if ("S".equals(bsType)) {
            double tax = round((amt * 0.003));
            return tax;
        } else if ("B".equals(bsType)) {
            return 0.0;
        }
        return 0.0;
    }

    public double countNetAmt(double amt, String bsType, double fee, double tax) {
        if ("S".equals(bsType)) {
            double netamt = round((amt - fee - tax));
            return netamt;
        } else if ("B".equals(bsType)) {
            double netamt = -round((amt + fee));
            return netamt;
        }
        return 0;
    }
    public double countProfitability(double unrealProfit,double cost){
        double profitability=unrealProfit/cost*100;
        return profitability;
    }


}
//    double sumProfitability = transactionMethodService.countProfitability(unrealInfo.getSumUnrealProfit(), unrealInfo.getSumCost());
//            unrealInfo.setSumProfitability(sumProfitability + "%");
//                    if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
//                    if (sumProfitability >= request.getProfitabilityLowerLimit()) {
//                    unrealSum.add(unrealInfo); //將物件放入最終要回傳的陣列中
//                    }
//                    } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
//                    if (sumProfitability <= request.getProfitabilityUpperLimit()) {
//                    unrealSum.add(unrealInfo); //將物件放入最終要回傳的陣列中
//
//                    }
//                    } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
//                    if (sumProfitability >= request.getProfitabilityLowerLimit() && sumProfitability <= request.getProfitabilityUpperLimit()) {
//                    unrealSum.add(unrealInfo); //將物件放入最終要回傳的陣列中
//                    }
//                    } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
//                    unrealSum.add(unrealInfo); //將物件放入最終要回傳的陣列中
//                    }
//
//                    double profitability = transactionMethodService.countProfitability(unrealInfo.getUnrealProfit(), unrealInfo.getCost());
//
//                    if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
//                    if (profitability >= request.getProfitabilityLowerLimit()) {
//                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
//                    }
//                    } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
//                    if (profitability <= request.getProfitabilityUpperLimit()) {
//                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
//
//                    }
//                    } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
//                    if (profitability >= request.getProfitabilityLowerLimit() && profitability <= request.getProfitabilityUpperLimit()) {
//                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
//                    }
//                    } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
//                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
//                    }

//    private String makeLastDocSeq(String tradeDate) {
//
//        String lastDocSeq = hcmioRepository.getLastDocSeq(tradeDate);
//        if (null == lastDocSeq) {
//            return "AA001";
//        }
//
//        int firstEngToAscii = lastDocSeq.charAt(0);
//        int secondEngToAscii = lastDocSeq.charAt(1);
//        String num = lastDocSeq.substring(2, 5);
//        int numToInt = Double.parseInt(num) + 1;
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
//
//        String numToString = String.format("%03d", numToInt);
//        String firstEngToString = Character.toString((char) firstEngToAscii);
//        String secondEngToString = Character.toString((char) secondEngToAscii);
//
//        String docSeq = firstEngToString + secondEngToString + numToString;
//
//        return docSeq;
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
//public String unrealizedGainOrLoss(String stock) {
//    Mstmb mstmb = mstmbRepository.findByStock(stock);
//    double qty = tcnudRepository.findByStock(stock).getRemainQty();
//    double amt = mstmb.getCurPrice() * qty;
//    double fee = amt * 0.001425;
//    double tax = amt * 0.003;
//    double unrealizedGainOrLoss = amt - fee - tax - tcnudRepository.findByStock(stock).getCost();
//    return Double.toString((int) unrealizedGainOrLoss);
//}
//}