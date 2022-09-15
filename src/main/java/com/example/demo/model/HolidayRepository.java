package com.example.demo.model;

import com.example.demo.model.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, String> {
    String findByHoliday(String holiday);
}
