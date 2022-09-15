package com.example.demo.model;

import com.example.demo.model.entity.Mstmb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MstmbRepository extends JpaRepository<Mstmb, String> {
    Mstmb findByStock(String stock);
}
