package com.example.demo.service;

import com.example.demo.controller.dto.request.CreateMstmbRequest;
import com.example.demo.controller.dto.request.StockInfoRequest;
import com.example.demo.controller.dto.request.UpdateMstmbRequest;
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
import java.util.List;

@Service
public class MstmbService {
    @Autowired
    MstmbRepository mstmbRepository;

    public StockInfoResponse createMstmb(CreateMstmbRequest request) {
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

    @Cacheable(value = "cacheStock", key = "#request.getStock")
    public StockInfoResponse getStockInfo(StockInfoRequest request) {
        StockInfoResponse stockInfoResponse=new StockInfoResponse();
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock());
        if (null == mstmb) {
            stockInfoResponse.setStatus("查無此檔股票資訊");
            return stockInfoResponse;
        }
        stockInfoResponse.setStatus("查詢成功");
        stockInfoResponse.setMstmb(mstmb);

        return stockInfoResponse;
    }

    @CachePut(value = "cacheStock", key = "#request.getStock")
    public StockInfoResponse updateMstmb(UpdateMstmbRequest request) {
        StockInfoResponse stockInfoResponse = new StockInfoResponse();
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock());
        mstmb.setCurPrice(request.getCurPrice());
        mstmb.setRefPrice(request.getCurPrice());
        mstmb.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        mstmb.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        mstmb.setModUser("Leo");

        mstmbRepository.save(mstmb);

        stockInfoResponse.setMstmb(mstmb);
        stockInfoResponse.setStatus("現值更新成功");
        return stockInfoResponse;
    }

}

//    public List<Mstmb> getAllMstmb() {
//        List<Mstmb> mstmbList = mstmbRepository.findAll();
//        return mstmbList;
//    }
