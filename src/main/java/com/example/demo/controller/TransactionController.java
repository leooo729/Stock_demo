package com.example.demo.controller;

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
@RequestMapping("/api/v1/unreal")
public class TransactionController {
    @Autowired
    TransactionService transactionService;

    @PostMapping("/add")
    public TransactionResponse makeTransaction(@RequestBody TransactionRequest transactionRequest) {
        TransactionResponse response= transactionService.makeTransaction(transactionRequest);
        return response;
    }

    @PostMapping("/detail")
    public TransactionResponse getUnrealDetail(@RequestBody UnrealRequest unrealRequest) {
        TransactionResponse response = transactionService.getUnrealDetail(unrealRequest);
        return response;
    }
    @PostMapping("/sum")
    public UnrealSumResponse getUnrealDetailSum(@RequestBody UnrealRequest unrealRequest) {
        UnrealSumResponse response= transactionService.getUnrealDetailSum(unrealRequest);
        return response;
    }


}
