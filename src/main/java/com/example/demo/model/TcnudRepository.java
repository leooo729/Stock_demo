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
    Tcnud findByStock(String stock);

    List findByBranchNoAndCustSeqAndStock(String branchNo,String custSeq,String stock);
    List findByBranchNoAndCustSeq(String branchNo,String custSeq);
    Tcnud findByTradeDateAndBranchNoAndCustSeqAndDocSeq(String tradeDate,String branchNo,String CustSeq,String Docseq);


    @Query(value = "select distinct stock from tcnud where branchNo= ?1 AND custSeq= ?2",nativeQuery = true)
    List<String> findDistinctStock(String branchNo,String custSeq);


}