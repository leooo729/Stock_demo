package com.example.demo.service;

import com.example.demo.controller.dto.request.DeliveryFeeRequest;
import com.example.demo.controller.dto.request.TransactionRequest;
import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.*;
import com.example.demo.model.HcmioRepository;
import com.example.demo.model.HolidayRepository;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.TcnudRepository;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@Transactional
public class TransactionService {

    @Autowired
    HcmioRepository hcmioRepository;
    @Autowired
    TcnudRepository tcnudRepository;
    @Autowired
    MstmbRepository mstmbRepository;
    @Autowired
    TransactionMethodService transactionMethodService;
    @Autowired
    HolidayRepository holidayRepository;

    public TransactionResponse makeTransaction(TransactionRequest request) {

        //輸入資料檢查有誤，回傳有誤
        if (!"allPass".equals(transactionMethodService.checkTransactionRequest(request))) {
            return new TransactionResponse(null, "002", transactionMethodService.checkTransactionRequest(request));
        }
//----------------------------------------------------------------------------------------
        //創建存放新交易明細檔的物件
        Hcmio hcmio = new Hcmio();
        //將資料放入交易明細檔
        hcmio.setTradeDate(request.getTradeDate());
        hcmio.setBranchNo(request.getBranchNo());
        hcmio.setCustSeq(request.getCustSeq());
        hcmio.setDocSeq(request.getDocSeq());
        hcmio.setStock(request.getStock());
        hcmio.setBsType("B");
        hcmio.setPrice(request.getPrice());
        hcmio.setQty(request.getQty());
        hcmio.setAmt(transactionMethodService.countAmt(hcmio.getPrice(), hcmio.getQty()));
        hcmio.setFee(transactionMethodService.countFee(hcmio.getAmt()));
        hcmio.setTax(transactionMethodService.countTax(hcmio.getAmt(), hcmio.getBsType()));
        hcmio.setStinTax(0.0);
        hcmio.setNetAmt(transactionMethodService.countNetAmt(hcmio.getAmt(), hcmio.getBsType(), hcmio.getFee(), hcmio.getTax()));
        hcmio.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        hcmio.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        hcmio.setModUser("Leo");
        //將交易明細檔做儲存
        hcmioRepository.save(hcmio);
//----------------------------------------------------------------------------------------
        //藉由上方交易明細檔的資料去創建現股餘額資料檔
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
        tcnud.setCost((int) Math.abs(hcmio.getNetAmt()));
        tcnud.setModDate(hcmio.getModDate());
        tcnud.setModTime(hcmio.getModTime());
        tcnud.setModUser(hcmio.getModUser());

        tcnudRepository.save(tcnud);//將現股餘額資料檔做儲存
// ----------------------------------------------------------------------------------------
        UnrealDetail unrealDetail = transactionMethodService.getUnrealDetail(tcnud); //用傳入值去跑Method來產生一新物件
        List<UnrealDetail> unrealDetails = new ArrayList<>(); //創一個陣列，用來存放上方新建物件
        unrealDetails.add(unrealDetail); //物件放入陣列

        return new TransactionResponse(unrealDetails, "000", "");
    }

    public TransactionResponse getUnrealDetail(UnrealRequest request) {
        //如有誤，回傳錯誤原因
        if ("查無符合資料".equals(transactionMethodService.checkUnrealRequest(request))) {
            return new TransactionResponse(null, "001", "查無符合資料");
        } else if (!"allPass".equals(transactionMethodService.checkUnrealRequest(request))) {
            return new TransactionResponse(null, "002", transactionMethodService.checkUnrealRequest(request));
        }

        List<Tcnud> tcnudList; //宣吿一新存餘額表的陣列

        if (request.getStock().isEmpty()) { //輸入無股票代碼，抓該用戶所有的餘額表資料
            tcnudList = tcnudRepository.findByBranchNoAndCustSeq(request.getBranchNo(), request.getCustSeq());
        } else { //抓該用戶，輸入代碼的股票的所有餘額表資料
            tcnudList = tcnudRepository.findByBranchNoAndCustSeqAndStock(request.getBranchNo(), request.getCustSeq(), request.getStock());
        }

        List<UnrealDetail> unrealDetails = new ArrayList<>(); //創建一存放未實現損益明細的陣列

        for (Tcnud tcnud : tcnudList) { //用餘額表陣列去跑迴圈
            UnrealDetail unrealDetail = transactionMethodService.getUnrealDetail(tcnud); //用餘額表資料去創建未實現損益明細物件
// ----------------------------------------------------------------------------------------
            //獲利率區間判斷
            double profitability = transactionMethodService.countProfitability(unrealDetail.getUnrealProfit(), unrealDetail.getCost());

            if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //只限制下限
                if (profitability >= request.getProfitabilityLowerLimit()) {
                    unrealDetails.add(unrealDetail);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //只限制上限
                if (profitability <= request.getProfitabilityUpperLimit()) {
                    unrealDetails.add(unrealDetail);
                }
            } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //限制上下範圍
                if (profitability >= request.getProfitabilityLowerLimit() && profitability <= request.getProfitabilityUpperLimit()) {
                    unrealDetails.add(unrealDetail);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //拿所有
                unrealDetails.add(unrealDetail);
            }
        }
// ----------------------------------------------------------------------------------------
        if (unrealDetails.isEmpty()) { //避免篩選完出現沒有資料的情況
            return new TransactionResponse(null, "001", "查無符合資料");
        }
        return new TransactionResponse(unrealDetails, "000", "");
    }

    public UnrealSumResponse getUnrealDetailSum(UnrealRequest request) {

        //如有誤，回傳錯誤原因
        if ("查無符合資料".equals(transactionMethodService.checkUnrealRequest(request))) {
            return new UnrealSumResponse(null, "001", "查無符合資料");
        } else if (!"allPass".equals(transactionMethodService.checkUnrealRequest(request))) {
            return new UnrealSumResponse(null, "002", transactionMethodService.checkUnrealRequest(request));
        }

        List<String> distinctStockList;
        if (request.getStock().isEmpty()) { //取得該用戶有購買的所有股票(沒有重複值)
            distinctStockList = tcnudRepository.findDistinctStock(request.getBranchNo(), request.getCustSeq());
        } else { //只拿request傳入的股票
            distinctStockList = new ArrayList<>();
            distinctStockList.add(request.getStock());
        }

        List<UnrealSum> unrealSumList = new ArrayList<>(); //創建一存放未實現損益明細總和的陣列

        for (String stock : distinctStockList) { //用使用者所持股票去跑迴圈
            Mstmb mstmb = mstmbRepository.findByStock(stock); //抓到目前股票代碼的資料明細檔
            //創建一存放目前股票的未實現損益明細的陣列，當換下一個股票時，在new一新的，不同股票才可分開
            UnrealRequest getUnrealDetail = new UnrealRequest(request.getBranchNo(), request.getCustSeq(),stock, null, null);
            List<UnrealDetail> unrealDetailList = getUnrealDetail(getUnrealDetail).getResultList(); //將同檔股票的未實現損益明細放入同一陣列

            UnrealSum unrealSum = new UnrealSum(); //創建一新未實現損益明細總和的物件

            for (UnrealDetail unrealDetail : unrealDetailList) { //跑目前迴圈股票的每一個未實現損益明細的陣列做數字的累加
                    unrealSum.setSumRemainQty(null == unrealSum.getSumRemainQty() ? unrealDetail.getRemainQty() : unrealSum.getSumRemainQty() + unrealDetail.getRemainQty());
                    unrealSum.setSumFee(null == unrealSum.getSumFee() ? unrealDetail.getFee() : unrealSum.getSumFee() + unrealDetail.getFee());
                    unrealSum.setSumCost(null == unrealSum.getSumCost() ? unrealDetail.getCost() : unrealSum.getSumCost() + unrealDetail.getCost());
                    unrealSum.setSumMarketValue(null == unrealSum.getSumMarketValue() ? unrealDetail.getMarketValue() : unrealSum.getSumMarketValue() + unrealDetail.getMarketValue());
            }

            unrealSum.setStock(stock);
            unrealSum.setStockName(mstmb.getStockName());
            unrealSum.setNowPrice(String.format("%.2f", transactionMethodService.makeRoundTwo(mstmb.getCurPrice())));
            unrealSum.setSumUnrealProfit(unrealSum.getSumMarketValue() - unrealSum.getSumCost());
            unrealSum.setProfitability(String.format("%.2f", transactionMethodService.makeRoundTwo(unrealSum.getSumUnrealProfit() / (double) unrealSum.getSumCost() * 100)) + "%");
            unrealSum.setDetaiList(unrealDetailList); //將小迴圈所儲存的未實現損益明細陣列，也丟進物件中

            double Profitability = transactionMethodService.countProfitability(unrealSum.getSumUnrealProfit(), unrealSum.getSumCost());
// ----------------------------------------------------------------------------------------
            //獲利率區間判斷
            if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //只限制下限
                if (Profitability >= request.getProfitabilityLowerLimit()) {
                    unrealSumList.add(unrealSum);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //只限制上限
                if (Profitability <= request.getProfitabilityUpperLimit()) {
                    unrealSumList.add(unrealSum);
                }
            } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //限制上下範圍
                if (Profitability >= request.getProfitabilityLowerLimit() && Profitability <= request.getProfitabilityUpperLimit()) {
                    unrealSumList.add(unrealSum);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //拿所有
                unrealSumList.add(unrealSum);
            }
        }
// ----------------------------------------------------------------------------------------
        if (unrealSumList.isEmpty()) { //避免篩選完出現沒有資料的情況
            return new UnrealSumResponse(null, "001", "查無符合資料");
        }
        return new UnrealSumResponse(unrealSumList, "000", "");
    }

    public String getDeliveryFee(DeliveryFeeRequest request) { //計算交割金
        if (!"allPass".equals(transactionMethodService.checkDeliveryFeeRequest(request))) { //有誤，回傳錯誤訊息
            return transactionMethodService.checkDeliveryFeeRequest(request);
        }

        Calendar today = Calendar.getInstance(); //抓今日
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd"); //設定輸出格式，給資料庫做查詢

        if (null != holidayRepository.findByHoliday(sdf.format(today.getTime())) || 1 == today.get(Calendar.DAY_OF_WEEK) || 7 == today.get(Calendar.DAY_OF_WEEK))
            return "今天是假日，無需付交割金";

        int workday = 0; //今天日期跟購買日期中間需要兩個工作天，用0開始，往前一天推，如果前一天不是假日，＋1，當到達2代表已有兩天工作天，也就可以抓到購買日期
        while (workday < 2) {
            today.add(Calendar.DATE, -1);
            if (null != holidayRepository.findByHoliday(sdf.format(today.getTime())) || 1 != today.get(Calendar.DAY_OF_WEEK) || 7 != today.get(Calendar.DAY_OF_WEEK)) {
                workday++;
            }
        }

        Long deliveryFee = tcnudRepository.getDeliveryFee(request.getBranchNo(), request.getCustSeq(),sdf.format(today.getTime()));
        if (null == deliveryFee) {
            return "今日無交割金需付";
        }
        return "今日需付交割金為 : " + deliveryFee +" 元";
    }

}
