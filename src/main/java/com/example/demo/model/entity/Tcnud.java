package com.example.demo.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="tcnud")
@IdClass(TcnudRelationPK.class)
public class Tcnud {
    @Id
    @Column(name="TradeDate")
    private String tradeDate;
    @Id
    @Column(name="BranchNo")
    private String branchNo;
    @Id
    @Column(name="CustSeq")
    private String custSeq;
    @Id
    @Column(name="DocSeq")
    private String docSeq;
    @Column(name="Stock")
    private String stock;
    @Column(name="Price")
    private Double price;
    @Column(name="Qty")
    private Double qty;
    @Column(name="RemainQty")
    private Double remainQty;
    @Column(name="Fee")
    private Integer fee;
    @Column(name="Cost")
    private Integer cost;
    @Column(name="ModDate")
    private String modDate;
    @Column(name="ModTime")
    private String modTime;
    @Column(name="ModUser")
    private String modUser;


}
