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

    @Cacheable(value = "cacheStock", key = "#request.getStock()")
    public StockInfoResponse getStockInfo(StockInfoRequest request) {
        StockInfoResponse stockInfoResponse=new StockInfoResponse();
        if(!"allPass".equals(checkStockInfoRequest(request))){ //檢查傳入資料是否有誤
            return new StockInfoResponse(checkStockInfoRequest(request),null);
        }
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock()); //抓資料庫該檔股票資料
        stockInfoResponse.setStatus("查詢成功");
        stockInfoResponse.setMstmb(mstmb);

        return stockInfoResponse;
    }

    @CachePut(value = "cacheStock", key = "#request.getStock()")
    public StockInfoResponse updateStockPrice(UpdateStockPriceRequest request) {
        StockInfoResponse stockInfoResponse = new StockInfoResponse();

        if(!"allPass".equals(checkUpdatePriceRequest(request))){ //檢查傳入資料是否有誤
            return new StockInfoResponse(checkUpdatePriceRequest(request),null);
        }
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock()); //找到該檔股票在資料庫中的資料
        //做表更新
        mstmb.setCurPrice(request.getCurPrice());
        mstmb.setRefPrice(request.getCurPrice());
        mstmb.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        mstmb.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        mstmb.setModUser("Leo");

        mstmbRepository.save(mstmb); //將原本資料更新

        stockInfoResponse.setMstmb(mstmb);
        stockInfoResponse.setStatus("現值更新成功");
        return stockInfoResponse;
    }

    public StockInfoResponse createStockInfo(CreateStockInfoRequest request) {
        StockInfoResponse stockInfoResponse = new StockInfoResponse();
        Mstmb mstmb = new Mstmb();
        mstmb.setStock(request.getStock());
        mstmb.setStockName(request.getStockName());
        mstmb.setMarketType(request.getMarketType());
        mstmb.setCurPrice(request.getCurPrice());
        mstmb.setRefPrice(request.getRefPrice());
        mstmb.setCurrency(request.getCurrency());
        mstmb.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        mstmb.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        mstmb.setModUser("Leo");

        mstmbRepository.save(mstmb);

        stockInfoResponse.setMstmb(mstmb);
        stockInfoResponse.setStatus("股票資訊新建成功");
        return stockInfoResponse;
    }
    //--------------------------------------------------------------
    private String checkStockInfoRequest(StockInfoRequest request) {
        if (request.getStock().isBlank()) {
            return "請填寫欲查詢股票代碼";
        }
        if (null == mstmbRepository.findByStock(request.getStock())) {
            return "查無此檔股票資訊";
        }
        return "allPass";
    }
    private String checkUpdatePriceRequest(UpdateStockPriceRequest request) {
        if (request.getStock().isBlank()||null==request.getCurPrice()){
            return "表單未填寫完成";
        }
        if(0>=request.getCurPrice()){
            return "請輸入有效股票價格";
        }
        if(null==mstmbRepository.findByStock(request.getStock())){
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
