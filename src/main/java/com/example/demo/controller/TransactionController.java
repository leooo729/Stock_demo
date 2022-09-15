package com.example.demo.controller;

import com.example.demo.controller.dto.request.DeliveryFeeRequest;
import com.example.demo.controller.dto.request.TransactionRequest;
import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.TransactionResponse;
import com.example.demo.controller.dto.response.UnrealSumResponse;
import com.example.demo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @PostMapping("/unreal/add")
    public TransactionResponse makeTransaction(@RequestBody TransactionRequest transactionRequest) {
        TransactionResponse response= transactionService.makeTransaction(transactionRequest);
        return response;
    }

    @PostMapping("/unreal/detail")
    public TransactionResponse getUnrealDetail(@RequestBody UnrealRequest unrealRequest) {
        TransactionResponse response = transactionService.getUnrealDetail(unrealRequest);
        return response;
    }
    @PostMapping("/unreal/sum")
    public UnrealSumResponse getUnrealDetailSum(@RequestBody UnrealRequest unrealRequest) {
        UnrealSumResponse response= transactionService.getUnrealDetailSum(unrealRequest);
        return response;
    }
    @PostMapping("/deliveryfee")
    public String getDeliveryFee(@RequestBody DeliveryFeeRequest deliveryFeeRequest) {
        String response= transactionService.getDeliveryFee(deliveryFeeRequest);
        return response;
    }





}
