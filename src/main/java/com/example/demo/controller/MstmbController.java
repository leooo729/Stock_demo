package com.example.demo.controller;

import com.example.demo.controller.dto.request.CreateHcmioRequest;
import com.example.demo.controller.dto.request.CreateMstmbRequest;
import com.example.demo.controller.dto.response.StatusResponse;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import com.example.demo.service.MstmbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mstmb")
public class MstmbController {

    @Autowired
    private MstmbService mstmbService;

    @GetMapping()
    public List<Mstmb> getAllMstmb(){
        List<Mstmb> mstmbList=mstmbService.getAllMstmb();
        return mstmbList;
    }

    @GetMapping("/{stock}")
    public Mstmb getStockInfo(@PathVariable String stock){
        Mstmb mstmb=mstmbService.getStockInfo(stock);
        return mstmb;
    }
    @PostMapping()
    public StatusResponse createMstmb(@RequestBody CreateMstmbRequest request) {
        String response = mstmbService.createMstmb(request);
        return new StatusResponse(response);
    }
}
