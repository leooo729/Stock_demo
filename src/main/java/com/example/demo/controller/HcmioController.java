package com.example.demo.controller;

import com.example.demo.model.entity.Hcmio;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hcmio")
public class HcmioController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping()
    public List<Hcmio> getAllHcmio() {
        List<Hcmio> hcmioList = transactionService.getAllHcmio();
        return hcmioList;
    }

    @GetMapping("/{docSeq}")
    public Hcmio getByDocSeq(@PathVariable String docSeq) {
        Hcmio hcmio = transactionService.getByDocSeq(docSeq);
        return hcmio;
    }
}

//    @GetMapping()
//    public List<Tcnud>getAllTcnud(){
//        List<Tcnud> tcnudList=tcnudService.getAllTcnud();
//        return tcnudList;
//    }
//
//    @GetMapping("/{stock}")
//    public Tcnud getByDocSeq(@PathVariable String stock){
//        Tcnud tcnud=tcnudService.getByStock(stock);
//        return tcnud;
//    }
//
//    @GetMapping("/unrealizedGainOrLoss/{stock}")
//    public StatusResponse unrealizedGainOrLoss(@PathVariable String stock){
//        String response=tcnudService.unrealizedGainOrLoss(stock);
//        return new StatusResponse(response);
//    }

