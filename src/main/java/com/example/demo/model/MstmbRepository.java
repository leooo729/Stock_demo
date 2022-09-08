package com.example.demo.model;

import com.example.demo.model.entity.Mstmb;
import com.example.demo.model.entity.Tcnud;
import com.example.demo.model.entity.TcnudRelationPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstmbRepository extends JpaRepository<Mstmb, String> {
    Mstmb findByStock(String stock);
}
