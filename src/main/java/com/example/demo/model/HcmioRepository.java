package com.example.demo.model;

import com.example.demo.model.entity.Hcmio;
import com.example.demo.model.entity.HcmioRelationPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HcmioRepository extends JpaRepository<Hcmio, HcmioRelationPK> {
    //用來抓取指定日期，那一天最後產生的委託書號
    @Query(value = "select docSeq from hcmio where tradeDate =?1 order by tradeDate desc,docSeq desc limit 1;",nativeQuery = true)
    String getLastDocSeq(String tradeDate);
}

