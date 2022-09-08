package com.example.demo.service;

import com.example.demo.controller.dto.request.CreateHcmioRequest;
import com.example.demo.model.HcmioRepository;
import com.example.demo.model.MstmbRepository;
import com.example.demo.model.TcnudRepository;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HcmioService {

    @Autowired
    HcmioRepository hcmioRepository;
    @Autowired
    TcnudService tcnudService;
    @Autowired
    MstmbRepository mstmbRepository;
    @Autowired
    TcnudRepository tcnudRepository;


    public List<Hcmio> getAllHcmio() {
        List<Hcmio> hcmioList = hcmioRepository.findAll();
        return hcmioList;
    }

    public Hcmio getByDocSeq(String docSeq) {
        Hcmio hcmio = hcmioRepository.findByDocSeq(docSeq);
        return hcmio;
    }

    public String createHcmio(CreateHcmioRequest request) {
        //檢查是否有傳入值以及是否為正確格式
        //如有任一欄位未填，回傳有誤
        if (request.getStock().isBlank() || request.getStock().isEmpty() || request.getBsType().isBlank() || request.getBsType().isEmpty() || null == request.getQty()) {
            return "表單未填寫完成";
        }
        //輸入股票代碼在股票資訊檔中查不到，回傳有誤
        if (null == mstmbRepository.findByStock(request.getStock())) {
            return "未知股票";
        }
        //如輸入買賣行為(B,S)為小寫，轉成大寫格式，以供判讀
        if (request.getBsType().equals("s") || request.getBsType().equals("b")) {
            request.setBsType(request.getBsType().toUpperCase());
        }
        //買為行為輸入(b ,s ,B ,S)之外值，回傳有誤
        if (!request.getBsType().equals("S") && !request.getBsType().equals("B")) {
            return "需輸入B(買)或S(賣) (大小寫無影響)";
        }
        //如輸入不符合數量格式(數量<0 或 等於０)，回傳有誤
        if (request.getQty() <= 0) {
            return "請輸入有效股票數量";
        }
        //如檔股票確定有該檔股票餘額，但賣出的股數大於剩餘股數，回傳有誤
        if (null != tcnudRepository.findByStock(request.getStock())) {
            if ("S".equals(request.getBsType()) && tcnudRepository.findByStock(request.getStock()).getRemainQty() - request.getQty() < 0) {
                return "賣出數量超過手上持股，請輸入持股範圍：1 ~ " + String.format("%.0f", (tcnudRepository.findByStock(request.getStock()).getRemainQty()));
            }
        }
        //如果餘額表無該檔股票餘額，且使用者還執行賣出動作，回傳有誤
        if (null == tcnudRepository.findByStock(request.getStock()) && "S".equals(request.getBsType())) {
            return "無該檔持股";
        }
        //通過以上檢查，確定傳入的stock,bsType,qty都是有值且正確的

        //創建存放新交易明細檔的物件
        Hcmio hcmio = new Hcmio();
        //找到使用者欲操作股票的詳細資料
        Mstmb mstmb = mstmbRepository.findByStock(request.getStock());
        //將資料放入交易明細檔
        hcmio.setTradeDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        hcmio.setBranchNo("F62M");
        hcmio.setCustSeq("03");
        hcmio.setDocSeq(makeLastDocSeq(hcmio.getTradeDate()));

        hcmio.setStock(request.getStock());
        hcmio.setBsType(request.getBsType());
        hcmio.setPrice(mstmb.getCurPrice());
        hcmio.setQty(request.getQty());

        hcmio.setAmt(hcmio.countAmt(hcmio.getPrice(), hcmio.getQty()));
        hcmio.setFee(hcmio.countFee(hcmio.getAmt()));
        hcmio.setTax(hcmio.countTax(hcmio.getAmt(), hcmio.getBsType()));
        hcmio.setStinTax(0);
        hcmio.setNetAmt(hcmio.countNetAmt(hcmio.getAmt(), hcmio.getBsType(), hcmio.getFee(), hcmio.getTax()));
        hcmio.setModDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        hcmio.setModTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        hcmio.setModUser("Leo");
        //將交易明細檔做儲存
        hcmioRepository.save(hcmio);
        //藉由上方交易明細檔的資料去創建現股餘額資料檔
        tcnudService.createTcnud(hcmio);

        Tcnud tcnud = tcnudRepository.findByStock(hcmio.getStock());

        if (null == tcnud) {
            return "該持股已全數賣出";
        }

        return "交易成功";
    }

    private String makeLastDocSeq(String tradeDate) {

        String lastDocSeq = hcmioRepository.getLastDocSeq(tradeDate);
        if (null == lastDocSeq) {
            return "AA001";
        }

        int firstEngToAscii = lastDocSeq.charAt(0);
        int secondEngToAscii = lastDocSeq.charAt(1);
        String num = lastDocSeq.substring(2, 5);
        int numToInt = Integer.parseInt(num) + 1;


        if (numToInt > 999) {
            numToInt = 1;
            secondEngToAscii++;
            if (secondEngToAscii > 90) {
                secondEngToAscii = 65;
                firstEngToAscii++;
            }
        }

        String numToString = String.format("%03d", numToInt);
        String firstEngToString = Character.toString((char) firstEngToAscii);
        String secondEngToString = Character.toString((char) secondEngToAscii);

        String docSeq = firstEngToString + secondEngToString + numToString;

        return docSeq;
    }

}
