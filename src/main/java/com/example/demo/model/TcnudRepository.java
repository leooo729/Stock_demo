package com.example.demo.model;

import com.example.demo.model.entity.Tcnud;
import com.example.demo.model.entity.TcnudRelationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TcnudRepository extends JpaRepository<Tcnud, TcnudRelationPK> {

    List findByBranchNoAndCustSeqAndStock(String branchNo, String custSeq, String stock);

    List findByBranchNoAndCustSeq(String branchNo, String custSeq);

    Tcnud findByTradeDateAndBranchNoAndCustSeqAndDocSeq(String tradeDate, String branchNo, String CustSeq, String Docseq);

    @Query(value = "select distinct stock from tcnud where branchNo= ?1 AND custSeq= ?2", nativeQuery = true)
    List<String> findDistinctStock(String branchNo, String custSeq); //抓指定用戶的購買股票，重複不抓

    @Query(value = "select sum(cost) from tcnud where branchNo= ?1 AND custSeq= ?2 And tradeDate= ?3", nativeQuery = true)
    Double getDeliveryFee(String branchNo, String custSeq,String tradeDate); // 計算交割金額


}
