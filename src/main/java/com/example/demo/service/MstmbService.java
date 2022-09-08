package com.example.demo.service;

import com.example.demo.controller.dto.request.CreateMstmbRequest;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MstmbService {
    @Autowired
    MstmbRepository mstmbRepository;

    public List<Mstmb> getAllMstmb(){
        List<Mstmb> mstmbList=mstmbRepository.findAll();
        return mstmbList;
    }

    public Mstmb getStockInfo(String stock){
        Mstmb mstmb=mstmbRepository.findByStock(stock);
        return mstmb;
    }
    public String createMstmb(CreateMstmbRequest request){
        Mstmb mstmb=new Mstmb();
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

        return "股票資訊新建成功";
    }
}
