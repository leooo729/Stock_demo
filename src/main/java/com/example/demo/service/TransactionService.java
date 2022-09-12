package com.example.demo.service;

import com.example.demo.controller.dto.request.TransactionRequest;
import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.TransactionResponse;
import com.example.demo.controller.dto.response.UnrealDetail;
import com.example.demo.controller.dto.response.UnrealSum;
import com.example.demo.controller.dto.response.UnrealSumResponse;
import com.example.demo.model.HcmioRepository;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.TcnudRepository;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    HcmioRepository hcmioRepository;
    @Autowired
    TcnudRepository tcnudRepository;
    @Autowired
    MstmbRepository mstmbRepository;
    @Autowired
    TransactionMethodService transactionMethodService;

    public List<Hcmio> getAllHcmio() {
        List<Hcmio> hcmioList = hcmioRepository.findAll();
        return hcmioList;
    }

    public Hcmio getByDocSeq(String docSeq) {
        Hcmio hcmio = hcmioRepository.findByDocSeq(docSeq);
        return hcmio;
    }

    public TransactionResponse makeTransaction(TransactionRequest request) {
        //先創建一個最終回傳結果的物件
        TransactionResponse transactionResponse = new TransactionResponse();
        //輸入資料檢查有誤，回傳有誤
        if (!"allPass".equals(transactionMethodService.checkTransactionRequest(request))) {
            transactionResponse.setResultList(null);
            transactionResponse.setResponseCode("002");
            transactionResponse.setMessage(transactionMethodService.checkTransactionRequest(request));
            return transactionResponse;
        }
//----------------------------------------------------------------------------------------
        //創建存放新交易明細檔的物件
        Hcmio hcmio = new Hcmio();
        //將資料放入交易明細檔
        hcmio.setTradeDate(request.getTradeDate());//LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        hcmio.setBranchNo(request.getBranchNo());
        hcmio.setCustSeq(request.getCustSeq());
        hcmio.setDocSeq(request.getDocSeq());//makeLastDocSeq(hcmio.getTradeDate())

        hcmio.setStock(request.getStock());
        hcmio.setBsType("B");
        hcmio.setPrice(request.getPrice());//mstmb.getCurPrice()
        hcmio.setQty(request.getQty());

        hcmio.setAmt(transactionMethodService.countAmt(hcmio.getPrice(), hcmio.getQty()));
        hcmio.setFee(transactionMethodService.countFee(hcmio.getAmt()));
        hcmio.setTax(transactionMethodService.countTax(hcmio.getAmt(), hcmio.getBsType()));
        hcmio.setStinTax(0);
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
        tcnud.setCost(Math.abs(hcmio.getNetAmt()));
        tcnud.setModDate(hcmio.getModDate());
        tcnud.setModTime(hcmio.getModTime());
        tcnud.setModUser(hcmio.getModUser());

        tcnudRepository.save(tcnud);//將現股餘額資料檔做儲存
// ----------------------------------------------------------------------------------------
        UnrealDetail unrealDetail = transactionMethodService.getUnrealDetail(tcnud); //用傳入值去跑Method來產生一新物件

        List<UnrealDetail> unrealDetails = new ArrayList<>(); //創一個陣列，用來存放上方新建物件

        unrealDetails.add(unrealDetail); //物件放入陣列

        transactionResponse.setResultList(unrealDetails);
        transactionResponse.setResponseCode("000");
        transactionResponse.setMessage("");

        return transactionResponse;
    }

    public TransactionResponse getUnrealDetail(UnrealRequest request) {

        TransactionResponse transactionResponse = new TransactionResponse(); //先創建一個最終回傳結果的物件

        transactionMethodService.checkUnrealRequest(request); //輸入資料做檢查
        //如有誤，回傳錯誤原因
        if ("查無符合資料".equals(transactionMethodService.checkUnrealRequest(request))) {
            transactionResponse.setResultList(null);
            transactionResponse.setResponseCode("001");
            transactionResponse.setMessage("查無符合資料");

            return transactionResponse;
        } else if (!"allPass".equals(transactionMethodService.checkUnrealRequest(request))) {
            transactionResponse.setResultList(null);
            transactionResponse.setResponseCode("002");
            transactionResponse.setMessage(transactionMethodService.checkUnrealRequest(request));

            return transactionResponse;
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
            unrealDetails.add(unrealInfo); //上行所建物件放入陣列中
        }

        transactionResponse.setResultList(unrealDetails);
        transactionResponse.setResponseCode("000");
        transactionResponse.setMessage("");
        return transactionResponse;
    }

    public UnrealSumResponse getUnrealDetailSum(UnrealRequest request) {

        UnrealSumResponse unrealSumResponse = new UnrealSumResponse(); //先創建一個最終回傳結果的物件
        //如有誤，回傳錯誤原因
        if ("查無符合資料".equals(transactionMethodService.checkUnrealRequest(request))) {
            unrealSumResponse.setResultList(null);
            unrealSumResponse.setResponseCode("001");
            unrealSumResponse.setMessage("查無符合資料");

            return unrealSumResponse;
        } else if (!"allPass".equals(transactionMethodService.checkUnrealRequest(request))) {
            unrealSumResponse.setResultList(null);
            unrealSumResponse.setResponseCode("002");
            unrealSumResponse.setMessage(transactionMethodService.checkUnrealRequest(request));

            return unrealSumResponse;
        }
        //使用上面getUnrealDetail方法，先取得輸入值的未實現損益明細的陣列
        List<UnrealDetail> unrealDetails = getUnrealDetail(request).getResultList();
        //取得該用戶有購買的所有股票(沒有重複值)
        List<String> allUrealStock = tcnudRepository.findDistinctStock(request.getBranchNo(), request.getCustSeq());

        List<UnrealSum> unrealSum = new ArrayList<>(); //創建一存放未實現損益明細總和的陣列

        for (String stock : allUrealStock) { //用使用者所持股票去跑迴圈
            Mstmb mstmb = mstmbRepository.findByStock(stock); //抓到目前股票代碼的資料明細檔
            //創建一存放目前股票未實現損益明細的陣列，當換不同股票時，在new一新的，不同股票才可分開
            List<UnrealDetail> unrealDetailList = new ArrayList<>();

            double sumRemainQty = 0, sumFee = 0, sumCost = 0; //先初始化用來計算總和的參數
            for (UnrealDetail unrealDetail : unrealDetails) { //跑每一個未實現損益明細的陣列
                if (unrealDetail.getStock().equals(stock)) { //當有跟目前大迴圈 Stock 相同時進入條件
                    //做數字的累加
                    sumRemainQty += unrealDetail.getRemainQty();
                    sumFee += unrealDetail.getFee();
                    sumCost += unrealDetail.getCost();

                    unrealDetailList.add(unrealDetail); //將該未實現損益明細物件放入陣列
                }
            }
            UnrealSum unrealInfo = new UnrealSum(); //創建一新未實現損益明細總和的物件
            //將資料存入
            unrealInfo.setStock(stock);
            unrealInfo.setStockName(mstmb.getStockName());
            unrealInfo.setNowPrice(mstmb.getCurPrice());
            unrealInfo.setSumRemainQty(sumRemainQty);
            unrealInfo.setSumFee(sumFee);
            unrealInfo.setSumCost(sumCost);
            unrealInfo.setSumMarketValue(unrealInfo.getNowPrice() * unrealInfo.getSumRemainQty() - unrealInfo.getNowPrice() * unrealInfo.getSumRemainQty() * 0.003 - unrealInfo.getNowPrice() * unrealInfo.getSumRemainQty() * 0.001425);
            unrealInfo.setSumUnrealProfit(unrealInfo.getSumMarketValue() - unrealInfo.getSumCost());
            unrealInfo.setDetaiList(unrealDetailList); //將小迴圈所儲存的未實現損益明細陣列，也丟進物件中

            unrealSum.add(unrealInfo); //將物件放入最終要回傳的陣列中
        }

        unrealSumResponse.setResultList(unrealSum);
        unrealSumResponse.setResponseCode("000");
        unrealSumResponse.setMessage("");

        return unrealSumResponse;
    }

}
