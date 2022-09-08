package com.example.demo.controller;

import com.example.demo.controller.dto.request.UnrealRequest;
import com.example.demo.controller.dto.response.StatusResponse;
import com.example.demo.controller.dto.response.UnrealResponse;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Tcnud;
import com.example.demo.service.TcnudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tcnud")
public class TcnudController {

    @Autowired
    private TcnudService tcnudService;

    @GetMapping()
    public List<Tcnud>getAllTcnud(){
        List<Tcnud> tcnudList=tcnudService.getAllTcnud();
        return tcnudList;
    }

    @GetMapping("/{stock}")
    public Tcnud getByDocSeq(@PathVariable String stock){
        Tcnud tcnud=tcnudService.getByStock(stock);
        return tcnud;
    }
    @PostMapping()
    public StatusResponse createTcnud(Hcmio hcmio){
        String response=tcnudService.createTcnud(hcmio);
        return new StatusResponse(response);
    }
    @GetMapping("/unrealizedGainOrLoss/{stock}")
    public StatusResponse unrealizedGainOrLoss(@PathVariable String stock){
        String response=tcnudService.unrealizedGainOrLoss(stock);
        return new StatusResponse(response);
    }
    @PostMapping("/unreal/detail")
    public UnrealResponse unrealDetailResponse(@RequestBody UnrealRequest request){
        UnrealResponse response=tcnudService.getUnrealDetail(request);
        return response;
    }

    @PostMapping("/unreal/sum")
    public UnrealResponse getUnrealDetailSum(@RequestBody UnrealRequest request){
        UnrealResponse response=tcnudService.getUnrealDetailSum(request);
        return response;
    }




}
