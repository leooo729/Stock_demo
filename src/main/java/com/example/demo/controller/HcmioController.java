package com.example.demo.controller;

import com.example.demo.controller.dto.request.CreateHcmioRequest;
import com.example.demo.controller.dto.response.StatusResponse;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.service.HcmioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hcmio")
public class HcmioController {
    @Autowired
    private HcmioService hcmioService;

    @GetMapping()
    public List<Hcmio> getAllHcmio() {
        List<Hcmio> hcmioList = hcmioService.getAllHcmio();
        return hcmioList;
    }

    @GetMapping("/{docSeq}")
    public Hcmio getByDocSeq(@PathVariable String docSeq) {
        Hcmio hcmio = hcmioService.getByDocSeq(docSeq);
        return hcmio;
    }

    @PostMapping()
    public StatusResponse createHcmio(@RequestBody CreateHcmioRequest request) {
        String response = hcmioService.createHcmio(request);
        return new StatusResponse(response);
    }
}
