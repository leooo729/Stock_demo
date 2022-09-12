package com.example.demo.model;

import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.HcmioRelationPK;
import com.example.demo.model.entity.Tcnud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HcmioRepository extends JpaRepository<Hcmio, HcmioRelationPK> {
    Hcmio findByDocSeq(String docSeq);
    Hcmio findByStock(String stock);



    @Query(value = "select docSeq from hcmio where tradeDate =?1 order by tradeDate desc,docSeq desc limit 1;",nativeQuery = true)
    String getLastDocSeq(String tradeDate);
}

