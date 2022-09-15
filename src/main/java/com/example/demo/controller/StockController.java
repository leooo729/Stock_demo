package com.example.demo.controller;

import com.example.demo.controller.dto.request.CreateStockInfoRequest;
import com.example.demo.controller.dto.request.StockInfoRequest;
import com.example.demo.controller.dto.request.UpdateStockPriceRequest;
import com.example.demo.controller.dto.response.StockInfoResponse;
import com.example.demo.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mstmb")
public class StockController {

    @Autowired
    private StockService stockService;


    @PostMapping("/create")
    public StockInfoResponse createMstmb(@RequestBody CreateStockInfoRequest request) {
        StockInfoResponse response = stockService.createStockInfo(request);
        return response;
    }
    @PostMapping("/update")
    public StockInfoResponse updateMstmb(@RequestBody UpdateStockPriceRequest request) {
        StockInfoResponse response = stockService.updateStockPrice(request);
        return response;
    }
    @PostMapping("/stock")
    public StockInfoResponse getStockInfo(@RequestBody StockInfoRequest stockInfoRequest){
        StockInfoResponse response= stockService.getStockInfo(stockInfoRequest);
        return response;
    }
}


