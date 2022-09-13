package com.example.demo.controller;

import com.example.demo.controller.dto.request.CreateMstmbRequest;
import com.example.demo.controller.dto.request.UpdateMstmbRequest;
import com.example.demo.controller.dto.response.StockInfoResponse;
import com.example.demo.service.MstmbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mstmb")
public class MstmbController {

    @Autowired
    private MstmbService mstmbService;


    @PostMapping("/create")
    public StockInfoResponse createMstmb(@RequestBody CreateMstmbRequest request) {
        StockInfoResponse response = mstmbService.createMstmb(request);
        return response;
    }
    @PostMapping("/update")
    public StockInfoResponse updateMstmb(@RequestBody UpdateMstmbRequest request) {
        StockInfoResponse response = mstmbService.updateStockPrice(request);
        return response;
    }
}

//    @GetMapping()
//    public List<Mstmb> getAllMstmb(){
//        List<Mstmb> mstmbList=mstmbService.getAllMstmb();
//        return mstmbList;
//    }

