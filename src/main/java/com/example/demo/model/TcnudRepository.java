package com.example.demo.model;
import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.Tcnud;
import com.example.demo.model.entity.TcnudRelationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
@Repository
public interface TcnudRepository extends JpaRepository<Tcnud, TcnudRelationPK> {
    Tcnud findByDocSeq(String docSeq);
    Tcnud findByStock(String stock);

    List findByBranchNoAndCustSeqAndStock(String branchNo,String custSeq,String stock);
    List findByBranchNoAndCustSeq(String branchNo,String custSeq);

    @Query(value = "select distinct stock from tcnud where branchNo= ?1 AND custSeq= ?2",nativeQuery = true)
    List<String> findDistinctStock(String branchNo,String custSeq);

//    @Query(value = "SELECT * FROM tcnud WHERE tradeDate= ?1 AND branchNo= ?2 AND custSeq= ?3 AND docSeq= ?4",nativeQuery = true)
//    List<Tcnud> findByTradeDateAndBranchNoAndCustSeqAndDocSeq(String tradeDate, String branchNo,String custSeq,String docSeq);

//    @Modifying
//    @Transactional
//    @Query(value = "INSERT INTO tcnud(TradeDate, BranchNo, CustSeq, DocSeq, Stock, Price, Qty, RemainQty, Fee, Cost, ModDate, ModTime, ModUser) VALUES (:TradeDate, :BranchNo, :CustSeq, :DocSeq, :Stock, :Price, :Qty, :RemainQty, :Fee, :Cost, :ModDate, :ModTime, :ModUser) FROM mstmb",nativeQuery = true)
//    void CreateTcnud(@Param("TradeDate")String TradeDate, @Param("BranchNo")String BranchNo, @Param("CustSeq")String CustSeq, @Param("DocSeq")String DocSeq, @Param("Stock")String Stock, @Param("Price")double Price, @Param("Qty")double Qty, @Param("RemainQty")double RemainQty, @Param("Fee")double Fee, @Param("Cost")double Cost, @Param("ModDate")String ModDate, @Param("ModTime")String ModTime, @Param("ModUser")String ModUser);

//    @Modifying
//    @Transactional
//    @Query(value = "INSERT INTO tcnud(TradeDate, BranchNo, CustSeq, DocSeq, Stock, Price, Qty, RemainQty, Fee, Cost, ModDate, ModTime, ModUser) SELECT TradeDate,BranchNo, CustSeq, DocSeq, Stock, Price, Qty, Qty, Fee, NetAmt, ModDate, ModTime, ModUser FROM hcmio",nativeQuery = true)
//    void CreateTcnud(Hcmio hcmio);

//    @Param("TradeDate")String TradeDate, @Param("BranchNo")String BranchNo, @Param("CustSeq")String CustSeq, @Param("DocSeq")String DocSeq, @Param("Stock")String Stock, @Param("Price")double Price, @Param("Qty")double Qty, @Param("RemainQty")double RemainQty, @Param("Fee")double Fee, @Param("Cost")double Cost, @Param("ModDate")String ModDate, @Param("ModTime")String ModTime, @Param("ModUser")String ModUser

}
