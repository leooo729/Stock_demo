package com.example.demo.model.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class HcmioRelationPK implements Serializable {
    private String tradeDate;
    private String branchNo;
    private String custSeq;
    private String docSeq;
}
