package com.example.demo.service;

import com.example.demo.controller.dto.request.DeliveryFeeRequest;
import com.example.demo.controller.dto.request.TransactionRequest;
import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.TransactionResponse;
import com.example.demo.controller.dto.response.UnrealDetail;
import com.example.demo.controller.dto.response.UnrealSum;
import com.example.demo.controller.dto.response.UnrealSumResponse;
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
        hcmio.setTradeDate(request.getTradeDate()); //LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        hcmio.setBranchNo(request.getBranchNo());
        hcmio.setCustSeq(request.getCustSeq());
        hcmio.setDocSeq(request.getDocSeq()); //makeLastDocSeq(hcmio.getTradeDate())

        hcmio.setStock(request.getStock());
        hcmio.setBsType("B");
        hcmio.setPrice(request.getPrice());//mstmb.getCurPrice()
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

        List<Tcnud> tcnud; //宣吿一新存餘額表的陣列

        if (request.getStock().isEmpty()) { //輸入無股票代碼，抓該用戶所有的餘額表資料
            tcnud = tcnudRepository.findByBranchNoAndCustSeq(request.getBranchNo(), request.getCustSeq());
        } else { //抓該用戶，輸入代碼的股票的所有餘額表資料
            tcnud = tcnudRepository.findByBranchNoAndCustSeqAndStock(request.getBranchNo(), request.getCustSeq(), request.getStock());
        }


        List<UnrealDetail> unrealDetails = new ArrayList<>(); //創建一存放未實現損益明細的陣列

        for (Tcnud tcd : tcnud) { //用餘額表陣列去跑迴圈
            UnrealDetail unrealInfo = transactionMethodService.getUnrealDetail(tcd); //用餘額表資料去創建未實現損益明細物件
//----------------------------------
            double profitability = transactionMethodService.countProfitability(unrealInfo.getUnrealProfit(), unrealInfo.getCost());

            if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
                if (profitability >= request.getProfitabilityLowerLimit()) {
                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
                }
            } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
                if (profitability <= request.getProfitabilityUpperLimit()) {
                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
                }
            } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) {
                if (profitability >= request.getProfitabilityLowerLimit() && profitability <= request.getProfitabilityUpperLimit()) {
                    unrealDetails.add(unrealInfo); //所建物件放入陣列中
                }
            } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) {
                unrealDetails.add(unrealInfo); //所建物件放入陣列中
            }
        }

        if (unrealDetails.isEmpty()) {
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

        List<String> getDistinctStock;
        if (request.getStock().isEmpty()) { //取得該用戶有購買的所有股票(沒有重複值)
            getDistinctStock = tcnudRepository.findDistinctStock(request.getBranchNo(), request.getCustSeq());
        } else { //只拿request傳入的股票
            getDistinctStock = new ArrayList<>();
            getDistinctStock.add(request.getStock());
        }

        //使用上面getUnrealDetail方法，先取得輸入值的未實現損益明細的陣列
        UnrealRequest findAllStock = new UnrealRequest(request.getBranchNo(), request.getCustSeq(), request.getStock(), null, null);
        List<UnrealDetail> unrealDetailList = getUnrealDetail(findAllStock).getResultList();
        List<UnrealSum> unrealSums = new ArrayList<>(); //創建一存放未實現損益明細總和的陣列

        for (String stock : getDistinctStock) { //用使用者所持股票去跑迴圈
            String a=stock;
            Mstmb mstmb = mstmbRepository.findByStock(stock); //抓到目前股票代碼的資料明細檔
            //創建一存放目前股票未實現損益明細的陣列，當換不同股票時，在new一新的，不同股票才可分開
            List<UnrealDetail> unrealDetails = new ArrayList<>();
            UnrealSum unrealSum = new UnrealSum(); //創建一新未實現損益明細總和的物件

            for (UnrealDetail unrealDetail : unrealDetailList) { //跑每一個未實現損益明細的陣列
                if (unrealDetail.getStock().equals(stock)) { //當有跟目前大迴圈 Stock 相同時進入條件
                    //做數字的累加
                    unrealSum.setSumRemainQty(null == unrealSum.getSumRemainQty() ? unrealDetail.getRemainQty() : unrealSum.getSumRemainQty() + unrealDetail.getRemainQty());
                    unrealSum.setSumFee(null == unrealSum.getSumFee() ? unrealDetail.getFee() : unrealSum.getSumFee() + unrealDetail.getFee());
                    unrealSum.setSumCost(null == unrealSum.getSumCost() ? unrealDetail.getCost() : unrealSum.getSumCost() + unrealDetail.getCost());
                    unrealSum.setSumMarketValue(null == unrealSum.getSumMarketValue() ? unrealDetail.getMarketValue() : unrealSum.getSumMarketValue() + unrealDetail.getMarketValue());
                    //將同檔股票的未實現損益明細放入同一陣列
                    unrealDetails.add(unrealDetail);
                }
            }
            unrealSum.setStock(stock);
            unrealSum.setStockName(mstmb.getStockName());
            unrealSum.setNowPrice(String.format("%.2f",transactionMethodService.makeRoundTwo(mstmb.getCurPrice())));
            unrealSum.setSumUnrealProfit(unrealSum.getSumMarketValue() - unrealSum.getSumCost());

            unrealSum.setProfitability(String.format("%.2f",transactionMethodService.makeRoundTwo(unrealSum.getSumUnrealProfit()/(double)unrealSum.getSumCost()*100)) + "%");
            unrealSum.setDetaiList(unrealDetails); //將小迴圈所儲存的未實現損益明細陣列，也丟進物件中
            unrealSums.add(unrealSum); //將物件放入最終要回傳的陣列中
        }

        //用來篩選獲利率的結果
        List<UnrealSum> checkUnrealSum = new ArrayList<>(); //創建一存放r篩選完的未實現損益明細總和的陣列
        for (UnrealSum unrealSum : unrealSums) {
            double Profitability = transactionMethodService.countProfitability(unrealSum.getSumUnrealProfit(), unrealSum.getSumCost());
            if (null != request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //只限制下限
                if (Profitability >= request.getProfitabilityLowerLimit()) {
                    checkUnrealSum.add(unrealSum);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //只限制上限
                if (Profitability <= request.getProfitabilityUpperLimit()) {
                    checkUnrealSum.add(unrealSum); //將物件放入最終要回傳的陣列中

                }
            } else if (null != request.getProfitabilityLowerLimit() && null != request.getProfitabilityUpperLimit()) { //限制上下範圍
                if (Profitability >= request.getProfitabilityLowerLimit() && Profitability <= request.getProfitabilityUpperLimit()) {
                    checkUnrealSum.add(unrealSum);
                }
            } else if (null == request.getProfitabilityLowerLimit() && null == request.getProfitabilityUpperLimit()) { //拿所有
                checkUnrealSum.add(unrealSum); //將物件放入最終要回傳的陣列中
            }
        }
        if (checkUnrealSum.isEmpty()) { //避免篩選完出現沒有資料的情況
            return new UnrealSumResponse(null, "001", "查無符合資料");
        }
        return new UnrealSumResponse(checkUnrealSum, "000", "");
    }

    public String getDeliveryFee(DeliveryFeeRequest request) {

        Calendar today = Calendar.getInstance();
        if (1 == today.get(Calendar.DAY_OF_WEEK) || 7 == today.get(Calendar.DAY_OF_WEEK)) return "今天是假日，無需付交割金";

        Calendar target = Calendar.getInstance();
        target.add(Calendar.DATE, -2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        while (0 != today.compareTo(target)) {
            today.add(Calendar.DATE, -1);
            if (null != holidayRepository.findByHoliday(sdf.format(today.getTime())) || 1 == today.get(Calendar.DAY_OF_WEEK) || 7 == today.get(Calendar.DAY_OF_WEEK)) {
                target.add(Calendar.DATE, -1);
            }
        }
        Double deliveryFee = tcnudRepository.getDeliveryFee(request.getBranchNo(), request.getCustSeq(), sdf.format(target.getTime()));

        if (null == deliveryFee) return "今日無交割金需付";

        return "今日需付交割金為 : " + deliveryFee;
    }

}
