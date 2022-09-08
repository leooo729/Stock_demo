package com.example.demo.service;

import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.UnrealResponse;
import com.example.demo.controller.dto.response.UnrealDetail;
import com.example.demo.controller.dto.response.UnrealSum;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.TcnudRepository;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TcnudService {

    @Autowired
    private TcnudRepository tcnudRepository;
    @Autowired
    private MstmbRepository mstmbRepository;
//    @Autowired
//    private Mstmb mstmb;


    public List<Tcnud> getAllTcnud() {
        List<Tcnud> tcnudList = tcnudRepository.findAll();
        return tcnudList;
    }

    public Tcnud getByStock(String stock) {
        Tcnud tcnud = tcnudRepository.findByStock(stock);
        return tcnud;
    }

    public String createTcnud(Hcmio hcmio) {

        Tcnud tcnud = new Tcnud(); //創建存放新現股餘額資料檔的物件
        //將資料放入現股餘額資料檔
        tcnud.setTradeDate(hcmio.getTradeDate());
        tcnud.setBranchNo(hcmio.getBranchNo());
        tcnud.setCustSeq(hcmio.getCustSeq());
        tcnud.setDocSeq(hcmio.getDocSeq());
        tcnud.setStock(hcmio.getStock());
        tcnud.setPrice(hcmio.getPrice());
        tcnud.setQty(hcmio.getQty());
        tcnud.setRemainQty(hcmio.getQty());
        tcnud.setFee(hcmio.getFee());
        tcnud.setCost(Math.abs(hcmio.getNetAmt()));
        tcnud.setModDate(hcmio.getModDate());
        tcnud.setModTime(hcmio.getModTime());
        tcnud.setModUser(hcmio.getModUser());

        tcnudRepository.save(tcnud);//將現股餘額資料檔做儲存
        return "";
    }


    //    public List<>
    public UnrealResponse getUnrealDetail(UnrealRequest request) {

        List<Tcnud> tcnud;
        if (request.getStock().isEmpty()) {
            tcnud = tcnudRepository.findByBranchNoAndCustSeq(request.getBranchNo(), request.getCustSeq());
        } else {
            tcnud = tcnudRepository.findByBranchNoAndCustSeqAndStock(request.getBranchNo(), request.getCustSeq(), request.getStock());
        }

        UnrealResponse unrealResponse = new UnrealResponse();

        if (request.getBranchNo().isBlank() || request.getCustSeq().isBlank()) {//|| request.getStock().isBlank()
            unrealResponse.setResponseCode("002");
            unrealResponse.setMessage("參數檢核錯誤");
        } else if (tcnud.isEmpty()) {
            unrealResponse.setResponseCode("001");
            unrealResponse.setMessage("查無符合資料");
        } else {
            unrealResponse.setResponseCode("000");
            unrealResponse.setMessage("");
        }

        List<UnrealDetail> unrealDetail = new ArrayList<>();

        for (Tcnud tcd : tcnud) {
            Mstmb mstmb = mstmbRepository.findByStock(tcd.getStock());

            UnrealDetail unrealInfo = new UnrealDetail();

            unrealInfo.setTradeDate(tcd.getTradeDate());
            unrealInfo.setDocSeq(tcd.getDocSeq());
            unrealInfo.setStock(tcd.getStock());
            unrealInfo.setStockName(mstmb.getStockName());
            unrealInfo.setBuyPrice(tcd.getPrice());
            unrealInfo.setNowPrice(mstmb.getCurPrice());
            unrealInfo.setQty(tcd.getQty());
            unrealInfo.setRemainQty(tcd.getRemainQty());
            unrealInfo.setFee(tcd.getRemainQty() * tcd.getPrice() * 0.001425);
            unrealInfo.setCost(tcd.getCost());
            unrealInfo.setMarketValue(tcd.getRemainQty() * mstmb.getCurPrice() - tcd.getRemainQty() * mstmb.getCurPrice() * 0.003 - tcd.getRemainQty() * mstmb.getCurPrice() * 0.001425);
            unrealInfo.setUnrealProfit(unrealInfo.getMarketValue() - tcd.getCost());

            unrealDetail.add(unrealInfo);
        }

        unrealResponse.setResultList(unrealDetail);
        return unrealResponse;

    }

    public UnrealResponse getUnrealDetailSum(UnrealRequest request) {

        UnrealResponse unrealResponse = new UnrealResponse();

        List<UnrealDetail> unrealDetails = getUnrealDetail(request).getResultList();


        if (request.getBranchNo().isBlank() || request.getCustSeq().isBlank()) {// || request.getStock().isBlank()
            unrealResponse.setResponseCode("002");
            unrealResponse.setMessage("參數檢核錯誤");
            return unrealResponse;
        } else if (unrealDetails.isEmpty()) {
            unrealResponse.setResponseCode("001");
            unrealResponse.setMessage("查無符合資料");
            return unrealResponse;
        } else {
            unrealResponse.setResponseCode("000");
            unrealResponse.setMessage("");
        }

        List<String> allUrealStock = tcnudRepository.findDistinctStock(request.getBranchNo(), request.getCustSeq());

        List<UnrealSum> unrealSum = new ArrayList<>();

        for (String stock : allUrealStock) {
            Mstmb mstmb = mstmbRepository.findByStock(stock);
            List<UnrealDetail> unrealDetailList = new ArrayList<>();

            double sumRemainQty = 0, sumFee = 0, sumCost = 0, sumMarketValue = 0, sumUnrealProfit = 0;
            for (UnrealDetail unrealDetail : unrealDetails) {
                if (unrealDetail.getStock().equals(stock)) {
                    sumRemainQty += unrealDetail.getRemainQty();
                    sumFee += unrealDetail.getFee();
                    sumCost += unrealDetail.getCost();
                    sumMarketValue += unrealDetail.getMarketValue();
                    sumUnrealProfit += unrealDetail.getUnrealProfit();

                    unrealDetailList.add(unrealDetail);
                }
            }
            UnrealSum unrealInfo = new UnrealSum();
            unrealInfo.setStock(request.getStock());
            unrealInfo.setStockName(mstmb.getStockName());
            unrealInfo.setNowPrice(mstmb.getCurPrice());
            unrealInfo.setSumRemainQty(sumRemainQty);
            unrealInfo.setSumFee(sumFee);
            unrealInfo.setSumCos(sumCost);
            unrealInfo.setSumMarketValue(sumMarketValue);
            unrealInfo.setSumUnrealProfit(sumUnrealProfit);
            unrealInfo.setDetaiList(unrealDetailList);

            unrealSum.add(unrealInfo);
        }
        unrealResponse.setResultList(unrealSum);
        return unrealResponse;
    }


    public String unrealizedGainOrLoss(String stock) {
        Mstmb mstmb = mstmbRepository.findByStock(stock);
        double qty = tcnudRepository.findByStock(stock).getRemainQty();
        double amt = mstmb.getCurPrice() * qty;
        double fee = amt * 0.001425;
        double tax = amt * 0.003;
        double unrealizedGainOrLoss = amt - fee - tax - tcnudRepository.findByStock(stock).getCost();
        return Integer.toString((int) unrealizedGainOrLoss);
    }
}

//    public String createTcnud(Hcmio hcmio) {
//
//
//        if (null == tcnudRepository.findByStock(hcmio.getStock())) { //判斷交易明細檔傳入的股票代號，在原本的現股餘額資料檔中是否已做過交易
//            //如無
//            Tcnud tcnud = new Tcnud(); //創建存放新現股餘額資料檔的物件
//            //將資料放入現股餘額資料檔
//            tcnud.setTradeDate(hcmio.getTradeDate());
//            tcnud.setBranchNo(hcmio.getBranchNo());
//            tcnud.setCustSeq(hcmio.getCustSeq());
//            tcnud.setDocSeq(hcmio.getDocSeq());
//            tcnud.setStock(hcmio.getStock());
//            tcnud.setPrice(hcmio.getPrice());
//            tcnud.setQty(hcmio.getQty());
//            tcnud.setRemainQty(hcmio.getQty());
//            tcnud.setFee(hcmio.getFee());
//            tcnud.setCost(Math.abs(hcmio.getNetAmt()));
//            tcnud.setModDate(hcmio.getModDate());
//            tcnud.setModTime(hcmio.getModTime());
//            tcnud.setModUser(hcmio.getModUser());
//
//            tcnudRepository.save(tcnud);//將現股餘額資料檔做儲存
//        } else {  //如果交易明細檔傳入的股票代號，在原本的現股餘額資料檔中已做過交易
//
//            Tcnud tcnud = tcnudRepository.findByStock(hcmio.getStock());  //找到該檔股票的現股餘額資料檔資料
//
//            tcnud.setPrice(((tcnud.getQty() * tcnud.getPrice()) + hcmio.getAmt()) / (tcnud.getQty() + hcmio.getQty()));
//            tcnud.setQty(hcmio.getQty());
//            tcnud.setFee(tcnud.getFee() + hcmio.getFee());
//
//            tcnud.setModDate(hcmio.getModDate());
//            tcnud.setModTime(hcmio.getModTime());
//            tcnud.setModUser(hcmio.getModUser());
//
//            if ("S".equals(hcmio.getBsType())) {
//
//                tcnud.setRemainQty(tcnud.getRemainQty() - tcnud.getQty());
////                if (tcnud.getRemainQty()==0) {
////                    return "";
////                }
//                if (tcnud.getRemainQty() < 0) {
//                    return "";
//                }
//                tcnud.setCost(tcnud.getCost() - hcmio.getNetAmt());
//
//            } else if ("B".equals(hcmio.getBsType())) {
//
//                tcnud.setRemainQty(tcnud.getRemainQty() + tcnud.getQty());
//                tcnud.setCost(tcnud.getCost() + Math.abs(hcmio.getNetAmt()));
//            }
//            tcnudRepository.save(tcnud);
//        }
//        return "";
//    }


//T表刪除

//    public String createTcnud(Hcmio hcmio) {
//
//        Tcnud getNowTcnud = tcnudRepository.findByStock(hcmio.getStock());
//        if ("S".equals(hcmio.getBsType())) {
//            if (null != getNowTcnud && getNowTcnud.getRemainQty() - hcmio.getQty() == 0) {
//                tcnudRepository.delete(getNowTcnud);
//                return "";
//            }
//        }
//        Tcnud tcnud = new Tcnud();
//
//        tcnud.setTradeDate(hcmio.getTradeDate());
//        tcnud.setBranchNo(hcmio.getBranchNo());
//        tcnud.setCustSeq(hcmio.getCustSeq());
//        tcnud.setDocSeq(hcmio.getDocSeq());
//        tcnud.setStock(hcmio.getStock());
//        tcnud.setQty(hcmio.getQty());
//
//        if (null == getNowTcnud) {
//            tcnud.setPrice(hcmio.getPrice());
//            tcnud.setRemainQty(hcmio.getQty());
//            tcnud.setFee(hcmio.getFee());
//            tcnud.setCost(Math.abs(hcmio.getNetAmt()));
//        } else {
//
//            tcnud.setPrice(((getNowTcnud.getQty() * getNowTcnud.getPrice()) + hcmio.getAmt()) / (getNowTcnud.getQty() + hcmio.getQty()));
//            tcnud.setFee(getNowTcnud.getFee() + hcmio.getFee());
//
//            if ("S".equals(hcmio.getBsType())) {
//
//                tcnud.setRemainQty(getNowTcnud.getRemainQty() - tcnud.getQty());
//                tcnud.setCost(getNowTcnud.getCost() - hcmio.getNetAmt());
//
//            } else if ("B".equals(hcmio.getBsType())) {
//
//                tcnud.setRemainQty(getNowTcnud.getRemainQty() + tcnud.getQty());
//                tcnud.setCost(getNowTcnud.getCost() + Math.abs(hcmio.getNetAmt()));
//            }
//            tcnudRepository.delete(getNowTcnud);
//        }
//        tcnud.setModDate(hcmio.getModDate());
//        tcnud.setModTime(hcmio.getModTime());
//        tcnud.setModUser(hcmio.getModUser());
//
//        tcnudRepository.save(tcnud);
//        return "";
//    }


//    public UnrealResponse getUnrealDetailSum(UnrealRequest request) {
//
//        UnrealResponse unrealResponse = new UnrealResponse();
//
//
////        unrealResponse.setResultList(unrealDetails);
////        return unrealResponse;
//
//        List<UnrealDetail> unrealDetails = getUnrealDetail(request).getResultList();
//
//
//        if (request.getBranchNo().isBlank() || request.getCustSeq().isBlank()) {// || request.getStock().isBlank()
//            unrealResponse.setResponseCode("002");
//            unrealResponse.setMessage("參數檢核錯誤");
//            return unrealResponse;
//        } else if (unrealDetails.isEmpty()) {
//            unrealResponse.setResponseCode("001");
//            unrealResponse.setMessage("查無符合資料");
//            return unrealResponse;
//        } else {
//            unrealResponse.setResponseCode("000");
//            unrealResponse.setMessage("");
//        }
//
//        Mstmb mstmb = mstmbRepository.findByStock(request.getStock());
//        List<String> allUrealstock = tcnudRepository.findDistinctStock(request.getBranchNo(), request.getCustSeq());
//
////        List<UnrealDetail> a;
////
////        for (String stock : allUrealstock) {
////
////            for (UnrealDetail unrealDetail : unrealDetails) {
////                if (unrealDetail.getStock().equals(stock)) {
////                    a.add(unrealDetail);
////                }
////
////            }
//        List<UnrealSum> unrealSum = new ArrayList<>();
//
//        double sumRemainQty = 0, sumFee = 0, sumCost = 0, sumMarketValue = 0, sumUnrealProfit = 0;
//        for (UnrealDetail unrealDetail : unrealDetails) {
//            sumRemainQty += unrealDetail.getRemainQty();
//            sumFee += unrealDetail.getFee();
//            sumCost += unrealDetail.getCost();
//            sumMarketValue += unrealDetail.getMarketValue();
//            sumUnrealProfit += unrealDetail.getUnrealProfit();
//        }
//
//        UnrealSum unrealInfo = new UnrealSum();
//
//        unrealInfo.setStock(request.getStock());
//        unrealInfo.setStockName(mstmb.getStockName());
//        unrealInfo.setNowPrice(mstmb.getCurPrice());
//        unrealInfo.setSumRemainQty(sumRemainQty);
//        unrealInfo.setSumFee(sumFee);
//        unrealInfo.setSumCos(sumCost);
//        unrealInfo.setSumMarketValue(sumMarketValue);
//        unrealInfo.setSumUnrealProfit(sumUnrealProfit);
//        unrealInfo.setDetaiList(unrealDetails);
//
//        unrealSum.add(unrealInfo);
//
//        unrealResponse.setResultList(unrealSum);
//        return unrealResponse;
//    }
//
//