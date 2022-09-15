package com.example.demo.service;

import com.example.demo.controller.dto.request.CreateStockInfoRequest;
import com.example.demo.controller.dto.request.StockInfoRequest;
import com.example.demo.controller.dto.request.UpdateStockPriceRequest;
import com.example.demo.controller.dto.response.StockInfoResponse;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.entity.Mstmb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class StockService {
    @Autowired
    MstmbRepository mstmbRepository;
    @Autowired
    TransactionMethodService transactionMethodService;

    @Cacheable(value = "cacheStock", key = "#request.getStock()")
    public StockInfoResponse getStockInfo(StockInfoRequest request) {
        if (!"allPass".equals(checkStockInfoRequest(request))) { //檢查傳入資料是否有誤
            return new StockInfoResponse(checkStockInfoRequest(request), null);
        }
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock()); //抓資料庫該檔股票資料

        return new StockInfoResponse("查詢成功",mstmb);
    }

    @CachePut(value = "cacheStock", key = "#request.getStock()")
    public StockInfoResponse updateStockPrice(UpdateStockPriceRequest request) {

        if (!"allPass".equals(checkUpdatePriceRequest(request))) { //檢查傳入資料是否有誤
            return new StockInfoResponse(checkUpdatePriceRequest(request), null);
        }
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock()); //找到該檔股票在資料庫中的資料
        //做表更新
        mstmb.setCurPrice(transactionMethodService.makeRoundTwo(request.getCurPrice()));
        mstmb.setRefPrice(transactionMethodService.makeRoundTwo(request.getCurPrice()));
        mstmb.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        mstmb.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        mstmb.setModUser("Leo");

        mstmbRepository.save(mstmb); //將原本資料更新

        return new StockInfoResponse("現值更新成功",mstmb);
    }

    public StockInfoResponse createStockInfo(CreateStockInfoRequest request) {

        Mstmb mstmb = new Mstmb();
        mstmb.setStock(request.getStock());
        mstmb.setStockName(request.getStockName());
        mstmb.setMarketType(request.getMarketType());
        mstmb.setCurPrice(transactionMethodService.makeRoundTwo(request.getCurPrice()));
        mstmb.setRefPrice(transactionMethodService.makeRoundTwo(request.getRefPrice()));
        mstmb.setCurrency(request.getCurrency());
        mstmb.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        mstmb.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        mstmb.setModUser("Leo");

        mstmbRepository.save(mstmb);

        return new StockInfoResponse("股票資訊新建成功",mstmb);
    }

    //-------------------------------------------------------------- 檢查
    private String checkStockInfoRequest(StockInfoRequest request) {
        if (request.getStock().isBlank()) {
            return "查詢失敗，請填寫欲查詢股票代碼(長度需為4碼)";
        }
        if (null == mstmbRepository.findByStock(request.getStock())) {
            return "查無此檔股票資訊(長度需為4碼)";
        }
        return "allPass";
    }

    private String checkUpdatePriceRequest(UpdateStockPriceRequest request) {
        if (request.getStock().isBlank()||4!=request.getStock().length()) {
            return "更新失敗，請填寫欲更新股票代碼(長度需為4碼)";
        }
        if (0 >= request.getCurPrice()|| null == request.getCurPrice()) {
            return "請輸入有效股票價格";
        }
        if(request.getCurPrice()>=1_000_000){
            return "輸入價格超出範圍";
        }
        if (null == mstmbRepository.findByStock(request.getStock())) {
            return "查無該檔股票資料";
        }
        return "allPass";
    }
}

//    private String checkCreateStockInfoRequest(CreateStockInfoRequest request){
//        Mstmb stock=mstmbRepository.findByStock(request.getStock());
//        if(request.getStock().isBlank()){
//            return "股票代碼未填寫";
//        }
//        if(null!=stock){
//            return "該股票已存在資料庫，如需變更資料請去執行更新";
//        }
//        return "allPass";
//    }
//    public List<Mstmb> getAllMstmb() {
//        List<Mstmb> mstmbList = mstmbRepository.findAll();
//        return mstmbList;
//    }
